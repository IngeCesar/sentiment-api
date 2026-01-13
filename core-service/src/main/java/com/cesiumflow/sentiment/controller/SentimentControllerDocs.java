package com.cesiumflow.sentiment.controller;

import com.cesiumflow.sentiment.dto.BatchResponse;
import com.cesiumflow.sentiment.dto.DashboardStats;
import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * Especificación del contrato de interfaz (Contract-First).
 * Desacopla la definición del API de la implementación para garantizar la
 * consistencia del esquema ante los consumidores del ecosistema CesiumFlow.
 */
@Tag(name = "Sentiment Service", description = "Endpoints de inferencia NLP y agregación de telemetría.")
public interface SentimentControllerDocs {

    @Operation(summary = "Ejecución de inferencia NLP", description = "Orquesta el flujo de predicción asíncrona y persistencia. La respuesta garantiza trazabilidad mediante marcas de tiempo (ISO-8601) para sincronización de series temporales.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Análisis procesado e indexado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Violación de restricciones de validación en el payload"),
        @ApiResponse(responseCode = "500", description = "Falla de conectividad con el upstream de inferencia (Inference Engine)")
    })
    Mono<ResponseEntity<SentimentResponse>> analyzeSentiment(@RequestBody SentimentRequest request);

    /**
     * Nuevo endpoint para el procesamiento masivo de datos.
     * Utiliza FilePart para garantizar una lectura no bloqueante del flujo de datos.
     */
    @Operation(summary = "Procesamiento masivo vía CSV",
        description = "Recibe un archivo CSV, extrae el texto por líneas y orquesta la inferencia con concurrencia controlada. Ideal para ingesta masiva de feedback histórico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Archivo procesado exitosamente",
            content = @Content(schema = @Schema(implementation = BatchResponse.class))),
        @ApiResponse(responseCode = "415", description = "Tipo de medio no soportado (Se requiere multipart/form-data)"),
        @ApiResponse(responseCode = "500", description = "Error interno durante el streaming del archivo")
    })
    @PostMapping(value = "/sentiment/batch/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Mono<ResponseEntity<BatchResponse>> analyzeBatchCsv(
        @Parameter(description = "Archivo CSV con columna de texto para analizar")
        @RequestPart("file") FilePart file);

    @Operation(summary = "Recuperación de métricas agregadas", description = "Proporciona estados consolidados desde proyecciones SQL. Optimiza la carga del sistema al delegar el procesamiento de agregados a la capa de persistencia.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Snapshot de telemetría recuperado exitosamente")
    })
    Mono<ResponseEntity<DashboardStats>> getGlobalStats();

    @Operation(summary = "Verificación de trazabilidad", description = "Endpoint de diagnóstico para medir latencia y disponibilidad del puente de comunicación con el motor de IA.")
    Mono<ResponseEntity<SentimentResponse>> testConnection(
        @Parameter(description = "Texto de prueba para validación de respuesta del modelo") @RequestParam String text);

}
