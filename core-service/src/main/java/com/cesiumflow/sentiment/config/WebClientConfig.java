package com.cesiumflow.sentiment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

/**
 * Infraestructura de comunicación reactiva.
 * Define el cliente no bloqueante para la orquestación con el upstream de IA.
 */
@Configuration
public class WebClientConfig {

	@Value("${app.sentiment-engine.url}")
	private String engineUrl;

	/**
	 * Instancia única de WebClient configurada mediante Builder.
	 * Centraliza la URL base para garantizar la consistencia en el descubrimiento
	 * de servicios y facilitar la conmutación entre entornos.
	 * * @throws NullPointerException si la propiedad 'app.sentiment-engine.url' no
	 * está definida (fail-fast).
	 */
	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder
				.baseUrl(Objects.requireNonNull(engineUrl, "Upstream URL (Sentiment Engine) es requerida para el arranque"))
				.build();
	}
}