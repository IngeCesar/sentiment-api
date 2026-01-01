package com.cesiumflow.sentiment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder
				// We use the service name defined in docker-compose
				.baseUrl("http://sentiment-engine:5000")
				.build();
	}
}