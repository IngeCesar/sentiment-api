package com.cesiumflow.sentiment.exception;

import com.cesiumflow.sentiment.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Professional way to handle exceptions globally in WebFlux
public class GlobalExceptionHandler {

	/**
	 * Handles Validation Errors (like those from @Valid in SentimentRequest)
	 */
	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(WebExchangeBindException ex) {
		Map<String, String> errors = new HashMap<>();

		// Extract field-specific error messages
		ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		ErrorResponse errorResponse = ErrorResponse.builder()
				.status(HttpStatus.BAD_REQUEST.value())
				.message("Validation Failed")
				.timestamp(LocalDateTime.now())
				.details(errors)
				.build();

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handles general exceptions (The safety net)
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		ErrorResponse errorResponse = ErrorResponse.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.message("An unexpected error occurred: " + ex.getMessage())
				.timestamp(LocalDateTime.now())
				.build();

		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}