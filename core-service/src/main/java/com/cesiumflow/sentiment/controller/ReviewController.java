package com.cesiumflow.sentiment.controller;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import com.cesiumflow.sentiment.service.SentimentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*") // Allows requests from the Frontend (port 3000) or any other origin
public class ReviewController {

    private final SentimentService sentimentService;

    // Constructor Injection (Best Practice)
    public ReviewController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    // ==========================================
    // 1. Connectivity Test Endpoint (GET)
    // Usage: http://localhost:8080/api/reviews/test?text=Hello
    // Description: Simple endpoint to verify the full pipeline (Browser -> Java ->
    // Python)
    // ==========================================
    @GetMapping("/test")
    public SentimentResponse testConnection(@RequestParam String text) {
        return sentimentService.analyzeText(text);
    }

    // ==========================================
    // 2. Main Analysis Endpoint (POST)
    // Usage: POST http://localhost:8080/api/reviews/analyze
    // Body: Raw text string (e.g., "This product is amazing")
    // Description: Receives a review text, cleans it, and requests analysis from
    // the ML Service.
    // ==========================================
    @PostMapping("/analyze")
    public SentimentResponse analyzeReview(@RequestBody SentimentRequest request) {
        return sentimentService.analyzeText(request.getText());
    }
}