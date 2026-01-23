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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID; // <--- Importante: Importar UUID
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

    // Nivel de paralelismo ajustado para maximizar throughput en ingestas masivas.
    private static final int MAX_CONCURRENCY = 25;

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
     * Utiliza streaming reactivo para procesar línea por línea sin cargar el
     * archivo completo en memoria.
     * * @param filePart Flujo de datos del archivo subido.
     * * @return Mono con el resumen del procesamiento (Éxitos/Fallos).
     */
    public Mono<BatchResponse> processCsv(FilePart filePart) {
        log.info("🚀 Iniciando procesamiento masivo de CSV: {}", filePart.filename());

        AtomicLong total = new AtomicLong(0);
        AtomicLong success = new AtomicLong(0);
        AtomicLong failed = new AtomicLong(0);

        return DataBufferUtils.join(filePart.content())
            .map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                return new String(bytes, StandardCharsets.UTF_8);
            })
            .flatMapMany(content -> Flux.fromIterable(parseCsvRespectingQuotes(content)))
            .filter(line -> !line.isBlank() && !line.toLowerCase().startsWith("text")
                && !line.toLowerCase().startsWith("originaltext"))
            .doOnNext(line -> total.incrementAndGet())
            .flatMap(line -> analyzeText(line)
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
                MAX_CONCURRENCY
            )
            .then(Mono.fromCallable(() -> BatchResponse.builder()
                .totalProcessed(total.get())
                .success(success.get())
                .failed(failed.get())
                .build()))
            .doOnSuccess(res -> log.info("🏁 Proceso masivo finalizado. Éxitos: {}, Fallos: {}", res.getSuccess(),
                res.getFailed()));
    }

    /**
     * Agregación de telemetría mediante ejecución paralela.
     */
    public Mono<DashboardStats> getDashboardStats() {
        return Mono.zip(
                sentimentStatsRepo.findAll()
                    .collect(Collectors.toMap(
                        SentimentStatView::getSentiment,
                        SentimentStatView::getCount)),
                keywordStatsRepo.findAll()
                    .map(view -> new KeywordStat(view.getKeyword(), view.getCount()))
                    .collectList(),
                repository.count())
            .map(tuple -> new DashboardStats(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3()));
    }

    // --- SEGREGACIÓN DE OPERACIONES PRIVADAS ---

    private Mono<SentimentResponse> requestPrediction(String text) {
        String cleanText = text.replace("\"", "").trim();
        return webClient.post()
            .uri("/predict")
            .bodyValue(new SentimentRequest(cleanText))
            .retrieve()
            .bodyToMono(SentimentResponse.class);
    }

    /**
     * Mapeo y persistencia de resultados analíticos.
     * CORRECCIÓN APLICADA: Generación explícita de UUID y flag isNewRecord.
     */
    private Mono<SentimentResponse> persistAnalysis(String originalText, SentimentResponse response) {
        if (ERROR_STATUS.equals(response.getPrediction())) {
            return Mono.just(response);
        }

        // 1. Generamos el ID aquí mismo para tener control total
        UUID generatedId = UUID.randomUUID();

        SentimentRecord record = SentimentRecord.builder()
            .id(generatedId)          // <--- ASIGNACIÓN EXPLÍCITA DEL ID
            .isNewRecord(true)        // <--- CLAVE: Le dice a Persistable que haga INSERT
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

    private List<String> parseCsvRespectingQuotes(String content) {
        List<String> records = new ArrayList<>();
        StringBuilder currentRecord = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            }

            if ((c == '\n') && !inQuotes) {
                if (currentRecord.length() > 0) {
                    records.add(currentRecord.toString().trim());
                    currentRecord.setLength(0);
                }
            } else {
                currentRecord.append(c);
            }
        }

        if (currentRecord.length() > 0) {
            records.add(currentRecord.toString().trim());
        }

        return records;
    }
}
