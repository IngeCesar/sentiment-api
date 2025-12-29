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

    // Constructor Injection: Best practice for testability and immutability.
    // We inject the base URL defined in docker-compose.yml (env: SENTIMENT_API_URL)
    public SentimentService(RestTemplate restTemplate, 
                            @Value("${SENTIMENT_API_URL}") String sentimentApiUrl) {
        this.restTemplate = restTemplate;
        this.sentimentApiUrl = sentimentApiUrl;
    }

    /**
     * Sends the text to the Python microservice for analysis.
     * Includes error handling to prevent the Java app from crashing if Python is down.
     */
    public SentimentResponse analyzeText(String text) {
        // 1. Construct the full endpoint URL
        // The base URL comes from the environment variable (e.g., http://sentiment-engine:5000)
        String finalUrl = sentimentApiUrl + "/predict";
        
        // Log for debugging (visible in Docker console)
        System.out.println("🔄 Java sending request to: " + finalUrl);

        // 2. Create the request payload using the DTO
        SentimentRequest requestPayload = new SentimentRequest(text);

        try {
            // 3. Execute the HTTP POST request
            // RestTemplate automatically maps the JSON response to our SentimentResponse class
            return restTemplate.postForObject(finalUrl, requestPayload, SentimentResponse.class);
            
        } catch (Exception e) {
            // ERROR HANDLING: Graceful degradation
            // If the Python service is unreachable, we log the error and return a fallback response.
            System.err.println("❌ Error connecting to Python Service: " + e.getMessage());
            
            return new SentimentResponse(
                "CONNECTION_ERROR", 
                0.0, 
                Collections.emptyList(), 
                null
            );
        }
    }
}