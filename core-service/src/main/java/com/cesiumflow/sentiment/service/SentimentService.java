package com.cesiumflow.sentiment.service;

import com.cesiumflow.sentiment.dto.SentimentRequest;
import com.cesiumflow.sentiment.dto.SentimentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class SentimentService {

    private final RestTemplate restTemplate;
    private final String sentimentApiUrl;

    public SentimentService(RestTemplate restTemplate,
            @Value("${SENTIMENT_API_URL}") String sentimentApiUrl) {
        this.restTemplate = restTemplate;
        this.sentimentApiUrl = sentimentApiUrl;
    }

    public SentimentResponse analyzeText(String text) {
        // ✅ ARCHITECTURE ENFORCEMENT:
        // Java (Gateway) -> calls -> Python (Internal Engine)
        // Target: http://sentiment-engine:5000/predict
        String finalUrl = sentimentApiUrl + "/predict";

        System.out.println("🔄 [Internal] Java sending request to: " + finalUrl);

        SentimentRequest requestPayload = new SentimentRequest(text);

        try {
            return restTemplate.postForObject(finalUrl, requestPayload, SentimentResponse.class);
        } catch (Exception e) {
            System.err.println("❌ Error connecting to Python Service: " + e.getMessage());

            // Fallback response to prevent 500 Internal Server Error on the Frontend
            return new SentimentResponse(
                    "CONNECTION_ERROR",
                    0.0,
                    Collections.emptyList(),
                    null);
        }
    }
}