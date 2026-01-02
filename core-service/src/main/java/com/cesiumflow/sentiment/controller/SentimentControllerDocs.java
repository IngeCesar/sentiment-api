package com.cesiumflow.sentiment.controller;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@Tag(name = "Sentiment Analysis", description = "Endpoints for analyzing and persisting text sentiment")
public interface SentimentControllerDocs {

	@Operation(summary = "Analyze text sentiment", description = "Sends text to Python AI engine, gets prediction, and persists in PostgreSQL.")
	@ApiResponse(responseCode = "200", description = "Analysis successful")
	@ApiResponse(responseCode = "400", description = "Invalid input data")
	Mono<ResponseEntity<SentimentResponse>> analyzeSentiment(SentimentRequest request);

	@Operation(summary = "Connection test", description = "Quick connectivity test via GET request.")
	Mono<ResponseEntity<SentimentResponse>> testConnection(String text);
}