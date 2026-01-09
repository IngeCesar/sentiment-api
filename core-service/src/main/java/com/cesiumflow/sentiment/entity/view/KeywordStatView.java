package com.cesiumflow.sentiment.entity.view;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Mapeo de la Vista SQL 'view_top_keywords'.
 * Se utiliza para desacoplar el cálculo de estadísticas pesadas de la lógica de
 * Java,
 * delegando la agregación directamente al motor de PostgreSQL.
 */

@Data
@Table("view_top_keywords")
public class KeywordStatView {
	private String keyword;

	// Se usa Long para ser compatible con el tipo de retorno de COUNT(*) en SQL
	// y prevenir errores de desbordamiento en sets de datos masivos.
	private Long count;
}