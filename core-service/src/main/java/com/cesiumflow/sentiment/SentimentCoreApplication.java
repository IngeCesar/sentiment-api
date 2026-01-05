package com.cesiumflow.sentiment;

import com.cesiumflow.sentiment.entity.SentimentRecord; // Import necesario para el @see
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

/**
 * Punto de entrada del Microservicio Core de CesiumFlow.
 * Inicializa el contexto reactivo de Spring Boot y la configuración global.
 *
 * <p>
 * <strong>Configuración de Auditoría:</strong><br>
 * La anotación {@link EnableR2dbcAuditing} es mandatoria para habilitar el
 * {@code R2dbcAuditingEntityCallback}. Este componente es responsable de poblar
 * automáticamente los campos marcados con {@code @CreatedDate} antes de la
 * persistencia.
 * </p>
 *
 * @see SentimentRecord#getCreatedAt()
 */
@SpringBootApplication
@EnableR2dbcAuditing // Habilita la inyección automática de fechas (evita NULL en created_at)
public class SentimentCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(SentimentCoreApplication.class, args);
	}
}