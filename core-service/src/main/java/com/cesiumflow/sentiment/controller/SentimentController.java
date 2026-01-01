package com.cesiumflow.sentiment.controller;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import com.cesiumflow.sentiment.service.SentimentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@Tag(name = "Sentiment Analysis", description = "Endpoints for analyzing and persisting text sentiment")
public class SentimentController {

	private final SentimentService sentimentService;

	public SentimentController(SentimentService sentimentService) {
		this.sentimentService = sentimentService;
	}

	@Operation(summary = "Analyze text sentiment", description = "Sends the text to the Python AI engine, gets the prediction, and persists the result in PostgreSQL.")
	@ApiResponse(responseCode = "200", description = "Analysis successful and persisted")
	@ApiResponse(responseCode = "400", description = "Invalid input data")
	@PostMapping("/sentiment")
	public Mono<ResponseEntity<SentimentResponse>> analyzeSentiment(@Valid @RequestBody SentimentRequest request) {
		return sentimentService.analyzeText(request.getText())
				.map(ResponseEntity::ok);
	}

	@Operation(summary = "Connection test", description = "Quick test to verify the connection between Java and the Python engine via GET request.")
	@GetMapping("/test")
	public Mono<ResponseEntity<SentimentResponse>> testConnection(@RequestParam String text) {
		return sentimentService.analyzeText(text)
				.map(ResponseEntity::ok);
	}
}