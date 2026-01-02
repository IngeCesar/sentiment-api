package com.cesiumflow.sentiment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Sentiment Analysis Microservice (Core).
 * Bootstraps the Reactive Application Context.
 */

@SpringBootApplication
public class SentimentCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(SentimentCoreApplication.class, args);
	}
}
