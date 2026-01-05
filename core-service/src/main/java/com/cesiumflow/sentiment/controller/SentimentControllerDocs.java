package com.cesiumflow.sentiment.controller;

import com.cesiumflow.sentiment.dto.DashboardStats;
import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
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

	@Operation(summary = "Recuperación de métricas agregadas", description = "Proporciona estados consolidados desde proyecciones SQL. Optimiza la carga del sistema al delegar el procesamiento de agregados a la capa de persistencia.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Snapshot de telemetría recuperado exitosamente")
	})
	Mono<ResponseEntity<DashboardStats>> getGlobalStats();

	@Operation(summary = "Verificación de trazabilidad", description = "Endpoint de diagnóstico para medir latencia y disponibilidad del puente de comunicación con el motor de IA.")
	Mono<ResponseEntity<SentimentResponse>> testConnection(
			@Parameter(description = "Texto de prueba para validación de respuesta del modelo") @RequestParam String text);
}