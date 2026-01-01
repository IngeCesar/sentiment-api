package com.cesiumflow.sentiment.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("sentiment_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRecord {

	@Id
	private UUID id;
	@Column("original_text")
	private String originalText;

	private String prediction;

	private Double probability;

	@Column("created_at")
	private LocalDateTime createdAt;
}