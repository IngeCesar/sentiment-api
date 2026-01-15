package com.cesiumflow.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa el resumen de una operación de carga masiva.
 * Proporciona telemetría rápida sobre el éxito de la transacción.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchResponse {
    private long totalProcessed; // Total de líneas encontradas en el CSV
    private long success;        // Análisis persistidos correctamente
    private long failed;         // Errores de parsing o fallos del motor IA
}
