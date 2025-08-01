package com.swyp10.domain.recommendation.service;

import com.swyp10.domain.recommendation.entity.MonthlyRecommendation;
import com.swyp10.domain.recommendation.repository.MonthlyRecommendationRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyRecommendationService {

    private final MonthlyRecommendationRepository recommendationRepository;

    public MonthlyRecommendation getMonthlyRecommendation(Long festivalId) {
        return recommendationRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "MonthlyRecommendation not found: " + festivalId));
    }

    @Transactional
    public MonthlyRecommendation createMonthlyRecommendation(MonthlyRecommendation monthlyRecommendation) {
        return recommendationRepository.save(monthlyRecommendation);
    }

    @Transactional
    public void deleteMonthlyRecommendation(Long festivalId) {
        recommendationRepository.deleteById(festivalId);
    }
}
