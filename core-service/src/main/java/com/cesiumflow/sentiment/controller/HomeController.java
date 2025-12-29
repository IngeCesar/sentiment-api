package com.cesiumflow.sentiment.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*") // CORS configuration to allow requests from any origin
public class HomeController {

	// ==========================================
	// Root Health Check
	// Usage: http://localhost:8080/
	// Description: Simple message to confirm the container is alive.
	// ==========================================
	@GetMapping("/")
	public String home() {
		return "✅ Cesium Sentiment Core is running!";
	}
}