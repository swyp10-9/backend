package com.swyp10.domain.recommendation.repository;

import com.swyp10.domain.recommendation.entity.MonthlyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyRecommendationRepository extends JpaRepository<MonthlyRecommendation, Long> {
}
