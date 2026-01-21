package com.cesiumflow.sentiment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class CorsConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos los endpoints
            .allowedOriginPatterns("*") // PERMITE TODO (Para el concurso).
            // En producción real usaríamos .allowedOrigins("https://tu-frontend.vercel.app")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false) // Importante: false si usas "*" en origins
            .maxAge(3600);
    }
}
