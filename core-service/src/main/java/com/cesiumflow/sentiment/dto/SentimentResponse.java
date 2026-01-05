package com.cesiumflow.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

/**
 * Objeto de transferencia que representa el resultado final del análisis.
 * Estandariza la salida para el consumo del Frontend, desacoplando la respuesta
 * interna de Python de la estructura pública de la API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResponse {

    // Identificador único persistido (generado por PostgreSQL).
    // NOTA: Puede ser null si el análisis no se pudo guardar (ej: Fallo de
    // conexión).
    private UUID id;

    // Resultado categórico (ej: POSITIVO, NEGATIVO, NEUTRO)
    private String prediction;

    // Nivel de confianza del modelo (0.0 a 1.0)
    private Double probability;

    // Lista de términos clave para la nube de palabras
    private List<String> keywords;

    // Marca de tiempo oficial de persistencia (Source of Truth: Base de Datos).
    // Garantiza la sincronización cronológica del sistema.
    private String timestamp;
}