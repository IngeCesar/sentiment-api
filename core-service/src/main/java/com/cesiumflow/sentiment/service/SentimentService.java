package com.cesiumflow.sentiment.service;

import com.cesiumflow.sentiment.dto.*;
import com.cesiumflow.sentiment.entity.SentimentRecord;
import com.cesiumflow.sentiment.entity.view.SentimentStatView;
import com.cesiumflow.sentiment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Orquestador de dominio y lógica de negocio reactiva.
 * Gestiona la integración asíncrona entre el upstream de inferencia (Python)
 * y la persistencia relacional no bloqueante.
 * Actúa como la autoridad de sincronización de datos (Source of Truth).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentService {

    private static final String ERROR_STATUS = "CONNECTION_ERROR";

    private final WebClient webClient;
    private final SentimentRepository repository;
    private final SentimentStatsRepository sentimentStatsRepo;
    private final KeywordStatsRepository keywordStatsRepo;

    /**
     * Pipeline principal de procesamiento de texto.
     * Implementa encadenamiento reactivo para garantizar que la persistencia
     * sea atómica y dependiente de una predicción exitosa.
     *
     * @param text Texto crudo proveniente del cliente.
     * @return Mono con la respuesta enriquecida (Inferencia + Metadatos de BD).
     */
    public Mono<SentimentResponse> analyzeText(String text) {
        log.info("🔄 Iniciando orquestación de análisis: [{}]", text);

        return requestPrediction(text)
            .flatMap(response -> persistAnalysis(text, response))
            .onErrorResume(this::handleFallback);
    }

    /**
     * Procesa una ingesta masiva de datos mediante archivos CSV.
     * Utiliza streaming reactivo para procesar línea por línea sin cargar el archivo completo en memoria.
     * * @param filePart Flujo de datos del archivo subido.
     * @return Mono con el resumen del procesamiento (Éxitos/Fallos).
     */
    public Mono<BatchResponse> processCsv(FilePart filePart) {
        log.info("🚀 Iniciando procesamiento masivo de CSV: {}", filePart.filename());

        // Contadores atómicos para garantizar seguridad en hilos durante el flujo asíncrono.
        AtomicLong total = new AtomicLong(0);
        AtomicLong success = new AtomicLong(0);
        AtomicLong failed = new AtomicLong(0);

        return filePart.content()
            // Conversión de flujo de bytes (DataBuffer) a String de forma eficiente.
            .map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer); // Liberación manual para evitar fugas de memoria.
                return new String(bytes, StandardCharsets.UTF_8);
            })
            // Parsing: Dividir el contenido en líneas individuales.
            .flatMapIterable(content -> Arrays.asList(content.split("\\r?\\n")))
            // Filtro: Ignorar líneas vacías y cabeceras comunes del CSV.
            .filter(line -> !line.isBlank() && !line.toLowerCase().startsWith("text") && !line.toLowerCase().startsWith("originaltext"))
            .doOnNext(line -> total.incrementAndGet())
            // Orquestación: Procesamiento en paralelo con BACKPRESSURE (Máximo 2 peticiones activas).
            .flatMap(line ->
                    analyzeText(line) // Reutilizamos la lógica atómica existente.
                        .doOnNext(response -> {
                            if (ERROR_STATUS.equals(response.getPrediction())) {
                                failed.incrementAndGet();
                            } else {
                                success.incrementAndGet();
                            }
                        })
                        .onErrorResume(e -> {
                            failed.incrementAndGet();
                            return Mono.empty();
                        }),
                2 // Factor de concurrencia para proteger el motor de IA.
            )
            // Agregación final: Construir el resumen una vez que el flujo termine.
            .then(Mono.fromCallable(() -> BatchResponse.builder()
                .totalProcessed(total.get())
                .success(success.get())
                .failed(failed.get())
                .build()))
            .doOnSuccess(res -> log.info("🏁 Proceso masivo finalizado. Éxitos: {}, Fallos: {}", res.getSuccess(), res.getFailed()));
    }

    /**
     * Agregación de telemetría mediante ejecución paralela.
     * Utiliza 'Mono.zip' para optimizar el tiempo de respuesta total (RTT),
     * reduciendo la latencia al consolidar múltiples fuentes de datos en una única
     * emisión reactiva.
     */
    public Mono<DashboardStats> getDashboardStats() {
        return Mono.zip(
                // 1. Proyección de distribución de sentimientos
                sentimentStatsRepo.findAll()
                    .collect(Collectors.toMap(
                        SentimentStatView::getSentiment,
                        SentimentStatView::getCount)),
                // 2. Ranking de términos (Top-N keywords)
                keywordStatsRepo.findAll()
                    .map(view -> new KeywordStat(view.getKeyword(), view.getCount()))
                    .collectList(),
                // 3. Conteo volumétrico total
                repository.count())
            .map(tuple -> new DashboardStats(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3()));
    }

    // --- SEGREGACIÓN DE OPERACIONES PRIVADAS ---

    /**
     * Invocación al puente de inferencia NLP.
     * Realiza una llamada HTTP no bloqueante al motor interno de Python.
     */
    private Mono<SentimentResponse> requestPrediction(String text) {
        return webClient.post()
            .uri("/predict")
            .bodyValue(new SentimentRequest(text))
            .retrieve()
            .bodyToMono(SentimentResponse.class);
    }

    /**
     * Mapeo y persistencia de resultados analíticos.
     * Este método establece la "Verdad Única" del sistema: captura el UUID y
     * el Timestamp generados nativamente por la infraestructura (BD/Spring)
     * y los inyecta en el objeto de respuesta para garantizar consistencia.
     */
    private Mono<SentimentResponse> persistAnalysis(String originalText, SentimentResponse response) {
        if (ERROR_STATUS.equals(response.getPrediction())) {
            return Mono.just(response);
        }

        SentimentRecord record = SentimentRecord.builder()
            .originalText(originalText)
            .prediction(response.getPrediction())
            .probability(response.getProbability())
            .keywords(response.getKeywords() != null
                ? response.getKeywords().toArray(new String[0])
                : new String[0])
            .build();

        return repository.save(Objects.requireNonNull(record))
            .map(savedRecord -> {
                response.setId(savedRecord.getId());
                if (savedRecord.getCreatedAt() != null) {
                    response.setTimestamp(savedRecord.getCreatedAt().toString());
                }
                return response;
            })
            .doOnNext(resp -> log.info("✅ Registro persistido y sincronizado (ID: {})", resp.getId()));
    }

    /**
     * Mecanismo de Degradación Degradada (Graceful Degradation).
     * Garantiza la resiliencia del flujo ante fallos críticos (ej: Timeout de
     * Python),
     * retornando un estado de error controlado sin persistencia.
     */
    private Mono<SentimentResponse> handleFallback(Throwable e) {
        log.error("❌ Fallo crítico en el pipeline de análisis: {}", e.getMessage());

        return Mono.just(SentimentResponse.builder()
            .id(null)
            .prediction(ERROR_STATUS)
            .probability(0.0)
            .keywords(Collections.emptyList())
            .timestamp(null)
            .build());
    }
}
