package com.cesiumflow.sentiment.service;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import com.cesiumflow.sentiment.entity.SentimentRecord;
import com.cesiumflow.sentiment.repository.SentimentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentService {

    private static final String ERROR_STATUS = "CONNECTION_ERROR";
    private final WebClient webClient;
    private final SentimentRepository repository;

    /**
     * Orchestrates the main flow: Prediction -> Persistence -> Error Handling.
     * Uses reactive chaining to ensure non-blocking execution.
     *
     * @param text The input text to analyze.
     * @return A Mono containing the analysis result (or fallback error).
     */
    public Mono<SentimentResponse> analyzeText(String text) {
        log.info("🔄 Processing analysis for text: [{}]", text);

        return requestPrediction(text)
                .flatMap(response -> persistAnalysis(text, response))
                .onErrorResume(this::handleFallback);
    }

    /**
     * Step 1: Communication with AI Engine (Python).
     */
    private Mono<SentimentResponse> requestPrediction(String text) {
        return webClient.post()
                .uri("/predict")
                .bodyValue(new SentimentRequest(text))
                .retrieve()
                .bodyToMono(SentimentResponse.class);
    }

    /**
     * Step 2: Database Persistence (PostgreSQL).
     * Skips persistence if the prediction was already marked as an error.
     */
    private Mono<SentimentResponse> persistAnalysis(String originalText, SentimentResponse response) {
        // Defensive programming: Do not persist failed connection attempts
        if (ERROR_STATUS.equals(response.getPrediction())) {
            return Mono.just(response);
        }

        SentimentRecord record = SentimentRecord.builder()
                .originalText(originalText)
                .prediction(response.getPrediction())
                .probability(response.getProbability())
                .createdAt(resolveTimestamp(response.getTimestamp()))
                .build();

        return repository.save(Objects.requireNonNull(record))
                .doOnNext(saved -> log.info("✅ Persisted ID: {}", saved.getId()))
                .thenReturn(response);
    }

    /**
     * Step 3: Error Handling (Resilience).
     * Catches any upstream error and returns a safe fallback object.
     */
    private Mono<SentimentResponse> handleFallback(Throwable e) {
        log.error("❌ Critical Failure: {}", e.getMessage());
        // Return a safe, empty response to the client instead of crashing
        return Mono.just(new SentimentResponse(ERROR_STATUS, 0.0, Collections.emptyList(), null));
    }

    /**
     * Utility: Parses the timestamp string or defaults to server time if
     * invalid/null.
     */
    private LocalDateTime resolveTimestamp(String timestamp) {
        try {
            return (timestamp != null) ? LocalDateTime.parse(timestamp) : LocalDateTime.now();
        } catch (Exception e) {
            log.warn("⚠️ Invalid timestamp from engine, using server time.");
            return LocalDateTime.now();
        }
    }
}