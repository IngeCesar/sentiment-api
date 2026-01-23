package com.cesiumflow.sentiment.entity;

import lombok.*;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator; // Nuevo import
import org.springframework.data.annotation.Transient;        // Nuevo import
import org.springframework.data.domain.Persistable;          // Nuevo import
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Representa un registro de análisis en la tabla 'sentiment_records'.
 * Utiliza Spring Data R2DBC para persistencia reactiva en PostgreSQL.
 */

@Table("sentiment_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRecord implements Persistable<UUID> { // Implementamos Persistable

    // ID único generado automáticamente por JAVA.
    // Cambiamos la estrategia: Java genera el ID al crear el objeto,
    // así no dependemos de la configuración de la BD.
    @Id
    @Builder.Default // Asegura que el Builder respete esta generación automática
    private UUID id = UUID.randomUUID();

    private String originalText;

    private String prediction;

    private Double probability;

    /**
     * Se almacena como ARRAY nativo de PostgreSQL para optimizar el rendimiento.
     * Evita JOINs costosos y permite una recuperación atómica de datos para el
     * Dashboard.
     */
    private String[] keywords;

    // Auditoría: Spring llena este campo automáticamente antes de persistir.
    // Es la "Verdad Cronológica" del sistema.
    @CreatedDate
    private Instant createdAt;

    // --- NUEVOS CAMPOS Y LÓGICA PARA CONTROLAR EL ID ---

    // Campo interno para decirle a Spring si debe hacer INSERT o UPDATE.
    // @Transient significa que este campo NO existe en la base de datos.
    @Transient
    @Builder.Default
    private boolean isNewRecord = true;

    // Método obligatorio de la interfaz Persistable.
    // Spring llama a esto para decidir: ¿Hago INSERT (true) o UPDATE (false)?
    @Override
    public boolean isNew() {
        return isNewRecord;
    }

    /**
     * Constructor Mágico (@PersistenceCreator):
     * Este constructor es usado EXCLUSIVAMENTE por Spring cuando LEE datos de la base de datos.
     * Sirve para marcar el objeto como "No Nuevo" (isNewRecord = false) y evitar duplicados.
     */
    @PersistenceCreator
    public SentimentRecord(UUID id, String originalText, String prediction, Double probability, String[] keywords, Instant createdAt) {
        this.id = id;
        this.originalText = originalText;
        this.prediction = prediction;
        this.probability = probability;
        this.keywords = keywords;
        this.createdAt = createdAt;
        this.isNewRecord = false; // Si viene de la BD, ya existe, así que no es nuevo.
    }
}
