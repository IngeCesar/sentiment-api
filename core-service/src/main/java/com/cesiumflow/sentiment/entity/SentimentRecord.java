package com.cesiumflow.sentiment.entity;

import lombok.*;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Representa un registro de análisis en la tabla 'sentiment_records'.
 * Utiliza Spring Data R2DBC para persistencia reactiva en PostgreSQL.
 */

@Table("sentiment_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRecord {

	// ID único generado automáticamente por PostgreSQL (gen_random_uuid())
	// Spring detecta que es null al guardar y delega la generación a la BD.
	@Id
	private UUID id;

	private String originalText;

	private String prediction;

	private Double probability;

	/**
	 * Se almacena como ARRAY nativo de PostgreSQL para optimizar el rendimiento.
	 * Evita JOINs costosos y permite una recuperación atómica de datos para el
	 * Dashboard.
	 */
	private String[] keywords;

	// Auditoría: Spring llena este campo automáticamente antes de persistir.
	// Es la "Verdad Cronológica" del sistema.
	@CreatedDate
	private Instant createdAt;
}