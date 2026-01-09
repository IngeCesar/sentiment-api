package com.cesiumflow.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de transferencia para métricas de palabras clave individuales.
 * Se utiliza para mapear los resultados de las vistas SQL y enviarlos al
 * Dashboard.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeywordStat {

	// La palabra extraída por el motor de IA
	private String keyword;

	// Frecuencia de aparición. Se usa Long para mantener consistencia con
	// los tipos de agregación de la base de datos (COUNT).
	private Long count;
}