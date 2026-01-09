package com.cesiumflow.sentiment.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.cesiumflow.sentiment.entity.view.KeywordStatView;

public interface KeywordStatsRepository extends ReactiveCrudRepository<KeywordStatView, String> {
}