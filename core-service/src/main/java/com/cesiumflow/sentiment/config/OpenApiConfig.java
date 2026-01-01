package com.cesiumflow.sentiment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI (Swagger) documentation.
 * This class customizes the interactive API documentation for CesiumFlow.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI cesiumFlowOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("CesiumFlow - Sentiment Analysis API")
						.description("Reactive Microservice for real-time sentiment analysis. " +
								"Orchestrates communication between Python (AI Engine) and PostgreSQL.")
						.version("1.0.0")
						.contact(new Contact()
								.name("CesiumFlow Team")
								.url("https://github.com/cesiumflow")) // Your org URL
						.license(new License()
								.name("Apache 2.0")
								.url("http://springdoc.org")));
	}
}