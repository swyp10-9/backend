package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.entity.FestivalStatistics;
import com.swyp10.domain.festival.repository.FestivalStatisticsRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalStatisticsService {

    private final FestivalStatisticsRepository festivalStatisticsRepository;

    public FestivalStatistics getFestivalStatistics(Long festivalId) {
        return festivalStatisticsRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "FestivalStatistics not found: " + festivalId));
    }

    @Transactional
    public FestivalStatistics createFestivalStatistics(FestivalStatistics festivalStatistics) {
        return festivalStatisticsRepository.save(festivalStatistics);
    }

    @Transactional
    public void deleteFestivalStatistics(Long festivalId) {
        festivalStatisticsRepository.deleteById(festivalId);
    }
}
