package com.cesiumflow.sentiment.entity.view;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Mapeo de la Vista SQL 'view_sentiment_distribution'.
 * Proporciona la distribución global de sentimientos (POSITIVO, NEGATIVO,
 * NEUTRO)
 * ya procesada por la base de datos para alimentar los gráficos del Dashboard.
 */

@Data
@Table("view_sentiment_distribution")
public class SentimentStatView {
	private String sentiment;

	// Se utiliza Long por compatibilidad con las funciones de agregación de SQL
	// (COUNT)
	// y para asegurar la escalabilidad ante grandes volúmenes de análisis.
	private Long count;
}