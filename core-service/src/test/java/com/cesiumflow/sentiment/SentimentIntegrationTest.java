package com.cesiumflow.sentiment;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import com.cesiumflow.sentiment.repository.SentimentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de Integración del flujo completo de Sentimiento.
 * * Requiere que la infraestructura (PostgreSQL y Python AI Engine) esté
 * activa.
 * Valida desde la recepción HTTP hasta la persistencia final en DB.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SentimentIntegrationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private SentimentRepository repository;

	@Test
	@DisplayName("Debe analizar texto y persistir el resultado en la base de datos")
	void shouldAnalyzeAndPersistSentiment() {
		String testText = "Este test de integración es realmente útil.";
		SentimentRequest request = SentimentRequest.builder()
				.text(testText)
				.build();

		// 1. Fase de Acción (Capa HTTP)
		webTestClient.post()
				.uri("/api/v1/sentiment")
				.contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
				.bodyValue(Objects.requireNonNull(request))
				.exchange()
				.expectStatus().isOk()
				.expectBody(SentimentResponse.class)
				.consumeWith(result -> {
					SentimentResponse response = Objects.requireNonNull(result.getResponseBody(),
							"El cuerpo de la respuesta no puede ser nulo");

					assertThat(response).isNotNull();
					assertThat(response.getPrediction()).isNotNull();
				});

		// 2. Fase de Verificación (Capa de Datos)
		// Usamos StepVerifier para validar el flujo reactivo de la base de datos
		StepVerifier.create(repository.findAll())
				.thenConsumeWhile(record -> !record.getOriginalText().equals(testText))
				.expectNextMatches(record -> {
					return record.getOriginalText().equals(testText) && record.getId() != null;
				})
				.thenCancel()
				.verify();
	}

	@Test
	@DisplayName("Debe rechazar texto vacío con 400 Bad Request")
	void shouldRejectInvalidInput() {
		SentimentRequest invalidRequest = SentimentRequest.builder()
				.text("")
				.build();

		webTestClient.post()
				.uri("/api/v1/sentiment")
				// Fix: Enforce non-null body for invalid request scenario
				.bodyValue(Objects.requireNonNull(invalidRequest))
				.exchange()
				.expectStatus().isBadRequest(); // Valida que @Valid esté funcionando en el Controller
	}
}