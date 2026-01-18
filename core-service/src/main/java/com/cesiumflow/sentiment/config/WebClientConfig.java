package com.cesiumflow.sentiment.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Objects;

/**
 * Infraestructura de comunicación reactiva.
 * Define el cliente no bloqueante para la orquestación con el upstream de IA.
 */
@Configuration
public class WebClientConfig {

	@Value("${app.sentiment-engine.url}")
	private String engineUrl;

	@Value("${app.sentiment-engine.timeout:180000}")
	private int timeout;

	// Límite de 1MB alineado con la configuración de application.yml
	private static final int MAX_MEMORY_SIZE = 1 * 1024 * 1024;

	/**
	 * Instancia única de WebClient configurada mediante Builder.
	 * Centraliza la URL base para garantizar la consistencia en el descubrimiento
	 * de servicios y facilitar la conmutación entre entornos.
	 * * @throws NullPointerException si la propiedad 'app.sentiment-engine.url' no
	 * está definida (fail-fast).
	 */
	@Bean
	@SuppressWarnings("null")
	public WebClient webClient(WebClient.Builder builder) {
		// Validamos la creación del HttpClient para seguridad de tipos (elimina el
		// warning)
		HttpClient httpClient = Objects.requireNonNull(HttpClient.create(), "HttpClient no pudo inicializarse")
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.responseTimeout(Duration.ofMillis(timeout));

		return builder
				.baseUrl(Objects.requireNonNull(engineUrl, "Upstream URL (Sentiment Engine) es requerida para el arranque"))
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_MEMORY_SIZE))
				.build();
	}
}