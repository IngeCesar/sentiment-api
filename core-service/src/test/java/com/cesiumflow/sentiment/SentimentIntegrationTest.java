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

import java.util.Objects; // Required for strict null-safety checks

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test for the Sentiment Flow.
 *
 * Objectives:
 * 1. Verify that the Controller accepts HTTP requests.
 * 2. Ensure the Service communicates with the AI Engine (Python).
 * 3. Confirm that the Repository persists data to PostgreSQL.
 *
 * Note: Requires Docker containers (DB & Python) to be running.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SentimentIntegrationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private SentimentRepository repository;

	@Test
	@DisplayName("Should analyze text and persist the result in the database")
	void shouldAnalyzeAndPersistSentiment() {
		// 1. Arrange: Define the payload
		String testText = "Este test de integración es realmente útil.";
		SentimentRequest request = SentimentRequest.builder()
				.text(testText)
				.build();

		// 2. Act: Send POST request to the API
		webTestClient.post()
				.uri("/api/v1/sentiment")
				// Fix: Enforce non-null MediaType for strict type safety
				.contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
				// Fix: Enforce non-null Request body
				.bodyValue(Objects.requireNonNull(request))
				.exchange()

				// 3. Assert (HTTP Layer): Expect 200 OK
				.expectStatus().isOk()
				.expectBody(SentimentResponse.class)
				.consumeWith(result -> {
					// Fix: Safely unwrap response body. If null, test fails immediately here.
					SentimentResponse response = Objects.requireNonNull(result.getResponseBody(),
							"Response body must not be null");

					// AssertJ for logic validation
					assertThat(response).isNotNull();
					assertThat(response.getPrediction()).isNotNull();

					System.out.println("🧪 Test Response: " + response);
				});

		// 4. Assert (Data Layer): Verify persistence in PostgreSQL
		StepVerifier.create(repository.findAll())
				// Filter until we find our specific test record
				.thenConsumeWhile(record -> !record.getOriginalText().equals(testText))
				.expectNextMatches(record -> {
					System.out.println("✅ Found Record in DB with ID: " + record.getId());
					return record.getOriginalText().equals(testText) && record.getId() != null;
				})
				.thenCancel()
				.verify();
	}

	@Test
	@DisplayName("Should reject empty text with 400 Bad Request")
	void shouldRejectInvalidInput() {
		SentimentRequest invalidRequest = SentimentRequest.builder()
				.text("") // Empty text triggers validation error
				.build();

		webTestClient.post()
				.uri("/api/v1/sentiment")
				// Fix: Enforce non-null body for invalid request scenario
				.bodyValue(Objects.requireNonNull(invalidRequest))
				.exchange()
				.expectStatus().isBadRequest(); // Validates @NotBlank annotation
	}
}