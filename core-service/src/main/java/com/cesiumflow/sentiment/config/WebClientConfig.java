package com.cesiumflow.sentiment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

/**
 * Configuration for the Reactive WebClient.
 * Centralizes communication settings with external microservices.
 */
@Configuration
public class WebClientConfig {

	@Value("${app.sentiment-engine.url}")
	private String engineUrl;

	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder
				.baseUrl(Objects.requireNonNull(engineUrl, "Sentiment Engine URL cannot be null"))
				.build();
	}
}