package com.cesiumflow.sentiment.repository;

import com.cesiumflow.sentiment.entity.SentimentRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SentimentRepository extends ReactiveCrudRepository<SentimentRecord, UUID> {
}