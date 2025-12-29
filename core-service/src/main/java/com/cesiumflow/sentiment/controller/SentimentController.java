package com.cesiumflow.sentiment.controller;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import com.cesiumflow.sentiment.service.SentimentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1") // ✅ Best Practice: Versioning
@CrossOrigin(origins = "*")
public class SentimentController {

	private final SentimentService sentimentService;

	public SentimentController(SentimentService sentimentService) {
		this.sentimentService = sentimentService;
	}

	// ==========================================
	// MAIN ENDPOINT
	// URL: POST http://localhost:8080/api/v1/sentiment
	// ==========================================
	@PostMapping("/sentiment")
	public ResponseEntity<SentimentResponse> analyzeSentiment(@Valid @RequestBody SentimentRequest request) {
		// @Valid triggers the checks in SentimentRequest.java.
		// If valid, logic continues. If invalid, Spring returns 400 Bad Request
		// automatically.
		SentimentResponse response = sentimentService.analyzeText(request.getText());
		return ResponseEntity.ok(response);
	}

	// ==========================================
	// HEALTH CHECK / TEST
	// URL: GET http://localhost:8080/api/v1/test?text=Hello
	// ==========================================
	@GetMapping("/test")
	public ResponseEntity<SentimentResponse> testConnection(@RequestParam String text) {
		return ResponseEntity.ok(sentimentService.analyzeText(text));
	}
}