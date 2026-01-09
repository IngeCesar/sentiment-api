package com.cesiumflow.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estandariza las respuestas de falla para que el Frontend maneje errores
 * de forma predecible.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	private int status;
	private String message;

	// Facilita la correlación de errores entre el cliente y los logs del servidor
	private LocalDateTime timestamp;

	/**
	 * Mapa de detalles (ej. [campo: error]) para manejar múltiples fallas de
	 * validación en una sola respuesta.
	 */
	private Map<String, String> details;
}