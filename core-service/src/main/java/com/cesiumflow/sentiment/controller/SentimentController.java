package com.cesiumflow.sentiment.controller;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import com.cesiumflow.sentiment.service.SentimentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
// @CrossOrigin(origins = "*")
public class SentimentController implements SentimentControllerDocs {

	private final SentimentService sentimentService;

	public SentimentController(SentimentService sentimentService) {
		this.sentimentService = sentimentService;
	}

	@Override
	@PostMapping("/sentiment")
	public Mono<ResponseEntity<SentimentResponse>> analyzeSentiment(@Valid @RequestBody SentimentRequest request) {
		return sentimentService.analyzeText(request.getText())
				.map(ResponseEntity::ok);
	}

	@Override
	@GetMapping("/test")
	public Mono<ResponseEntity<SentimentResponse>> testConnection(@RequestParam String text) {
		return sentimentService.analyzeText(text)
				.map(ResponseEntity::ok);
	}
}