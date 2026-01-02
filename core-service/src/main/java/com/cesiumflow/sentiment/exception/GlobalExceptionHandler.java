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

@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handles Validation Errors (e.g., invalid JSON inputs).
	 * Uses Java Streams to map field errors cleanly.
	 */
	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(WebExchangeBindException ex) {
		Map<String, String> errors = ex.getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						// Value Mapper: Extract the message
						fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
						// If a key (field) repeats, join messages with "; "
						(existingMsg, newMsg) -> existingMsg + "; " + newMsg));

		return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
	}

	/**
	 * Handles unexpected general exceptions (The safety net).
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		// In a real production scenario, avoid sending ex.getMessage() to the client to
		// prevent data leaks.
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage(), null);
	}

	/**
	 * ATOMIC HELPER: Centralizes the response construction logic.
	 */
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