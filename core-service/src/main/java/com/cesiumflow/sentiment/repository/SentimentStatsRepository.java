package com.cesiumflow.sentiment.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.cesiumflow.sentiment.entity.view.SentimentStatView;

public interface SentimentStatsRepository extends ReactiveCrudRepository<SentimentStatView, String> {
}