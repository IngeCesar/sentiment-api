package com.cesiumflow.sentiment.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class SentimentHealthIndicator implements ReactiveHealthIndicator {

	private final WebClient webClient;

	public SentimentHealthIndicator(WebClient webClient) {
		this.webClient = webClient;
	}

	@Override
	public Mono<Health> health() {
		return webClient.get()
				.uri("/health") // O el endpoint /health que definamos en Python
				.retrieve()
				.toBodilessEntity() // Solo nos importa si responde (200 OK)
				.map(entity -> Health.up()
						.withDetail("service", "Sentiment Engine (Python)")
						.withDetail("status", "Reachable")
						.build())
				.onErrorResume(ex -> Mono.just(
						Health.down()
								.withDetail("service", "Sentiment Engine (Python)")
								.withDetail("error", "Unreachable: " + ex.getMessage())
								.build()));
	}
}