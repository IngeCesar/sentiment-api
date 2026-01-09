package com.cesiumflow.sentiment.controller;

import com.cesiumflow.sentiment.dto.DashboardStats;
import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import com.cesiumflow.sentiment.service.SentimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Orquestador de tráfico para el dominio de Sentimientos.
 * Implementa segregación de responsabilidades delegando la documentación
 * técnica (OpenAPI) a 'SentimentControllerDocs'.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
// Se omite @CrossOrigin porque el Reverse Proxy (Nginx/Gateway) centraliza
// la seguridad, evitando la dispersión de reglas de red en el código.
public class SentimentController implements SentimentControllerDocs {

	private final SentimentService sentimentService;

	/**
	 * Operación principal de inferencia de sentimientos.
	 * La naturaleza no bloqueante del Mono permite manejar ráfagas de tráfico
	 * (high throughput) sin saturar el pool de hilos del servidor.
	 */
	@Override
	@PostMapping("/sentiment")
	public Mono<ResponseEntity<SentimentResponse>> analyzeSentiment(@Valid @RequestBody SentimentRequest request) {
		return sentimentService.analyzeText(request.getText())
				.map(ResponseEntity::ok);
	}

	/**
	 * Agregador de métricas para consumo de Dashboard.
	 * Optimiza el rendimiento del cliente al consolidar estadísticas
	 * en una única transacción atómica, minimizando el overhead de red.
	 */
	@Override
	@GetMapping("/sentiment/stats")
	public Mono<ResponseEntity<DashboardStats>> getGlobalStats() {
		return sentimentService.getDashboardStats()
				.map(ResponseEntity::ok);
	}

	/**
	 * Endpoint de diagnóstico (Health Check extendido).
	 * Valida la trazabilidad y conectividad síncrona con el motor de inferencia
	 * NLP.
	 */
	@Override
	@GetMapping("/test")
	public Mono<ResponseEntity<SentimentResponse>> testConnection(@RequestParam String text) {
		return sentimentService.analyzeText(text)
				.map(ResponseEntity::ok);
	}
}
