package com.cesiumflow.sentiment.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Monitorea la disponibilidad del motor de IA externo.
 * Es crucial para la observabilidad, ya que el microservicio Core depende
 * directamente de la respuesta del motor en Python.
 */
@Component
@RequiredArgsConstructor
public class SentimentHealthIndicator implements ReactiveHealthIndicator {

	private static final String SERVICE_NAME = "Sentiment Engine (Python)";
	private static final String HEALTH_ENDPOINT = "/health";

	private final WebClient webClient;

	/**
	 * Ejecuta un chequeo no bloqueante hacia el motor de IA.
	 * Se utiliza 'toBodilessEntity()' para validar la conexión mediante el código
	 * de estado HTTP, evitando el costo innecesario de procesar un cuerpo de
	 * respuesta.
	 */
	@Override
	public Mono<Health> health() {
		return webClient.get()
				.uri(HEALTH_ENDPOINT)
				.retrieve()
				.toBodilessEntity() // Optimización: Solo nos interesa el status 200 OK
				.map(ignore -> buildUp())
				.onErrorResume(this::buildDown);
	}

	// Define el estado operativo cuando el puente hacia la IA está activo
	private Health buildUp() {
		return Health.up()
				.withDetail("service", SERVICE_NAME)
				.withDetail("status", "Reachable")
				.build();
	}

	/**
	 * Captura cualquier falla de red o timeout.
	 * Esto permite que Actuator marque el sistema como 'OUT_OF_SERVICE'
	 * si la dependencia crítica (IA) no responde.
	 */
	private Mono<Health> buildDown(Throwable ex) {
		return Mono.just(Health.down()
				.withDetail("service", SERVICE_NAME)
				.withDetail("error", "Unreachable: " + ex.getMessage())
				.build());
	}
}