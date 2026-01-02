package com.cesiumflow.sentiment.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("sentiment_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRecord {

	/**
	 * The unique ID of the record.
	 * Kept as NULL during creation; PostgreSQL generates it via
	 * 'gen_random_uuid()'.
	 */
	@Id
	private UUID id;

	private String originalText;

	private String prediction;

	private Double probability;

	private LocalDateTime createdAt;
}