package com.cesiumflow.sentiment.repository;

import com.cesiumflow.sentiment.entity.SentimentRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Reactive Repository for SentimentRecord.
 * It provides non-blocking CRUD operations out of the box.
 */
@Repository
public interface SentimentRepository extends ReactiveCrudRepository<SentimentRecord, UUID> {

	// Spring Data R2DBC will automatically implement this interface.
	// You already have:
	// - save(SentimentRecord) -> Mono<SentimentRecord>
	// - findById(UUID) -> Mono<SentimentRecord>
	// - findAll() -> Flux<SentimentRecord>
	// - deleteById(UUID) -> Mono<Void>
}