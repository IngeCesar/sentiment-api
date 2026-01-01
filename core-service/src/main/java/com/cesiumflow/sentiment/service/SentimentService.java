package com.cesiumflow.sentiment.service;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import com.cesiumflow.sentiment.entity.SentimentRecord;
import com.cesiumflow.sentiment.repository.SentimentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

@Service
public class SentimentService {

    private final WebClient webClient;
    private final SentimentRepository repository;

    public SentimentService(WebClient webClient, SentimentRepository repository) {
        this.webClient = webClient;
        this.repository = repository;
    }

    public Mono<SentimentResponse> analyzeText(String text) {
        System.out.println("🔄 [Reactive] Requesting Python engine...");

        return this.webClient.post()
                .uri("/predict")
                .bodyValue(new SentimentRequest(text))
                .retrieve()
                .bodyToMono(SentimentResponse.class)
                .flatMap(response -> saveAnalysis(text, response))
                .onErrorResume(e -> {
                    System.err.println("❌ Python Connection or DB failure: " + e.getMessage());
                    return Mono.just(new SentimentResponse("CONNECTION_ERROR", 0.0, Collections.emptyList(), null));
                });
    }

    private Mono<SentimentResponse> saveAnalysis(String originalText, SentimentResponse response) {
        if ("CONNECTION_ERROR".equals(response.getPrediction())) {
            return Mono.just(response);
        }

        SentimentRecord record = SentimentRecord.builder()
                .originalText(originalText)
                .prediction(response.getPrediction())
                .probability(response.getProbability())
                .createdAt(parseTimestamp(response.getTimestamp()))
                .build();

        return repository.save(Objects.requireNonNull(record))
                .doOnNext(saved -> {
                    // Postgres generates the ID, R2DBC retrieves it and gives it back to us here
                    System.out.println("✅ Analysis persisted. DB Generated ID: " + saved.getId());
                })
                .thenReturn(response);
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return (timestamp != null) ? LocalDateTime.parse(timestamp) : LocalDateTime.now();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}