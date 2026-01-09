package com.cesiumflow.sentiment.exception;

import com.cesiumflow.sentiment.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador global de excepciones de CesiumFlow.
 * Estandariza los errores para que el equipo de Frontend (Arnold) maneje
 * una estructura de datos predecible.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Captura errores de validación. Se utiliza 'WebExchangeBindException'
	 * por la naturaleza reactiva (WebFlux) del proyecto.
	 */
	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(WebExchangeBindException ex) {
		Map<String, String> errors = ex.getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Valor inválido",
						// Fusiona mensajes si un mismo campo tiene múltiples fallas de validación
						(existingMsg, newMsg) -> existingMsg + "; " + newMsg));

		return buildResponse(HttpStatus.BAD_REQUEST, "Falla en la validación de datos", errors);
	}

	/**
	 * Red de seguridad para excepciones no controladas.
	 * Nota: Se incluye el mensaje de la excepción para facilitar el debugging
	 * aunque en producción esto debería restringirse por seguridad.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno: " + ex.getMessage(), null);
	}

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, Map<String, String> details) {
		ErrorResponse response = ErrorResponse.builder()
				.status(status.value())
				.message(message)
				.timestamp(LocalDateTime.now())
				.details(details)
				.build();

		return ResponseEntity.status(status).body(response);
	}
}