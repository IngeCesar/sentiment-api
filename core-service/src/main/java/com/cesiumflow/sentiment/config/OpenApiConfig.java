package com.cesiumflow.sentiment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI (Swagger) documentation.
 * Customizes the interactive API documentation exposed at /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("CesiumFlow - Sentiment Analysis API")
						.description("Reactive Microservice orchestrating Python AI Engine and PostgreSQL persistence.")
						.version("1.0.0")
						.contact(new Contact()
								.name("CesiumFlow Team")
								.url("https://github.com/cesiumflow"))
						.license(new License()
								.name("MIT License")
								.url("https://opensource.org/licenses/MIT")));
	}
}