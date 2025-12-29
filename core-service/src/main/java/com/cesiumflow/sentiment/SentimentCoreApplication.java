package com.cesiumflow.sentiment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SentimentCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(SentimentCoreApplication.class, args);
	}

	@GetMapping("/")
    public String home() {
        return "Cesium Sentiment Core is running!";
    }

}
