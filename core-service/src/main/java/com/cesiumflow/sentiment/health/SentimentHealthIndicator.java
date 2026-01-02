package com.cesiumflow.sentiment.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SentimentHealthIndicator implements ReactiveHealthIndicator {

	private static final String SERVICE_NAME = "Sentiment Engine (Python)";
	private static final String HEALTH_ENDPOINT = "/health";

	private final WebClient webClient;

	@Override
	public Mono<Health> health() {
		return webClient.get()
				.uri(HEALTH_ENDPOINT)
				.retrieve()
				.toBodilessEntity()
				.map(ignore -> buildUp())
				.onErrorResume(this::buildDown);
	}

	/**
	 * ATOMIC HELPER: Constructs the Success Health object.
	 */
	private Health buildUp() {
		return Health.up()
				.withDetail("service", SERVICE_NAME)
				.withDetail("status", "Reachable")
				.build();
	}

	/**
	 * ATOMIC HELPER: Constructs the Failure Health object.
	 */
	private Mono<Health> buildDown(Throwable ex) {
		return Mono.just(Health.down()
				.withDetail("service", SERVICE_NAME)
				.withDetail("error", "Unreachable: " + ex.getMessage())
				.build());
	}
}