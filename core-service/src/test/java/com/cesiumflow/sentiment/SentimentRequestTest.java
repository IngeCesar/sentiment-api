package com.cesiumflow.sentiment.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Unitario para SentimentRequest (Ticket BE-03).
 * * TEMA: Aislamiento de Pruebas (Test Isolation)
 * OBJETIVO: Verificar las reglas de validación (@Size, @NotBlank) sin levantar 
 * el contexto completo de Spring Boot. Esto garantiza una ejecución 
 * extremadamente rápida (milisegundos).
 */
class SentimentRequestTest {

    // Motor de validación de Jakarta (Hibernate Validator)
    private Validator validator;

    /**
     * TEMA: Ciclo de Vida del Test (Test Lifecycle)
     * La anotación @BeforeEach asegura que este método se ejecute antes de cada @Test.
     * * MEJORA: Garantiza un estado limpio ("fresh state") para cada prueba,
     * evitando que los datos de un test afecten al siguiente.
     */
    @BeforeEach
    void setUp() {
        // Construcción manual de la fábrica de validación.
        // Esto simula el comportamiento de Spring Validation de forma ligera.
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailWhenTextIsTooShort() {
        // --- ARRANGE (Preparación) ---
        // TEMA: Pruebas de Límites (Boundary Testing)
        // Probamos el límite inferior definido en el DTO (@Size min=3).
        // Usamos "Hi" (2 chars) para provocar intencionalmente el fallo.
        SentimentRequest request = new SentimentRequest("Hi"); 

        // --- ACT (Acción) ---
        // Invocamos manualmente al validador para buscar violaciones a las reglas.
        Set<ConstraintViolation<SentimentRequest>> violations = validator.validate(request);

        // --- ASSERT (Verificación) ---
        // TEMA: Validación Negativa (Negative Testing)
        // Verificamos que la lista de violaciones NO esté vacía.
        // Es decir: "Esperamos que falle".
        assertFalse(violations.isEmpty(), "La validación debería fallar si el texto tiene menos de 3 caracteres");
        
        // (Opcional) TEMA: Especificidad del Error
        // Podríamos verificar también que el mensaje sea el correcto.
        // assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("entre 3 y 5000")));
    }

    @Test
    void shouldPassWhenTextIsValid() {
        // --- ARRANGE ---
        // TEMA: Happy Path (Camino Feliz)
        // Probamos con un dato que cumple todas las reglas (@NotBlank, @Size).
        SentimentRequest request = new SentimentRequest("Texto válido y correcto");

        // --- ACT ---
        Set<ConstraintViolation<SentimentRequest>> violations = validator.validate(request);

        // --- ASSERT ---
        // TEMA: Validación Positiva
        // Verificamos que NO existan errores (la lista debe estar vacía).
        assertTrue(violations.isEmpty(), "No deberían existir errores de validación para un texto correcto");
    }
}
