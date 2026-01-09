package com.cesiumflow.sentiment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de transferencia para recibir las solicitudes de análisis.
 * Define el contrato de validación antes de procesar el texto en el motor de
 * IA.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRequest {

    // Validamos que el texto no sea nulo ni vacío para evitar peticiones inútiles
    // al motor de IA.
    @NotBlank(message = "El campo 'text' es obligatorio.")
    // Límite de 5000 caracteres para asegurar la estabilidad del modelo de lenguaje
    // (NLP)
    // y prevenir ataques de denegación de servicio (DoS) por carga excesiva.
    @Size(min = 3, max = 5000, message = "El texto debe tener entre 3 y 5000 caracteres.")
    private String text;
}