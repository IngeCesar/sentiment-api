package com.cesiumflow.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Objeto de transferencia unificado para el Dashboard.
 * Agrupa todas las métricas necesarias para minimizar las peticiones HTTP
 * y mejorar la experiencia de usuario (UX).
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {

	/**
	 * Distribución de sentimientos para el gráfico circular (Donut Chart).
	 * Ejemplo: {"[POS]": 10, "[NEG]": 5, "[NEU]": 2}
	 */
	private Map<String, Long> sentimentDistribution;

	/**
	 * Top de palabras clave para el ranking o gráfico de barras.
	 * Se utiliza una lista de objetos KeywordStat para facilitar el mapeo en el
	 * frontend.
	 */
	private List<KeywordStat> topKeywords;

	// Contador global para la métrica principal del Dashboard.
	private Long totalAnalyzed;
}