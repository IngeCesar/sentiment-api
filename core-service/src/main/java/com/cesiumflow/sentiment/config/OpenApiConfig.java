package com.cesiumflow.sentiment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Definición del contrato de interfaz y especificación OpenAPI.
 * Establece el estándar de comunicación para consumidores de la capa Edge
 * y servicios externos, garantizando la consistencia del esquema de datos.
 */
@Configuration
public class OpenApiConfig {

	/**
	 * Configuración del manifiesto de la API.
	 * Centraliza la documentación técnica, políticas de licencia y
	 * puntos de contacto del ecosistema CesiumFlow.
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("CesiumFlow - Sentiment Analysis API")
						.description("Orquestador reactivo para el procesamiento asíncrono de feedback. " +
								"Abstrae la complejidad del motor de inferencia NLP y gestiona la persistencia relacional.")
						.version("1.0.0")
						.contact(new Contact()
								.name("CesiumFlow Team")
								.url("https://github.com/cesiumflow"))
						.license(new License()
								.name("MIT License")
								.url("https://opensource.org/licenses/MIT")));
	}
}