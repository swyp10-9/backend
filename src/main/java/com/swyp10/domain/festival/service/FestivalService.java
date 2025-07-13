package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.global.exception.ApplicationException;
import com.swyp10.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public Festival getFestival(Long festivalId) {
        return festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "Festival not found: " + festivalId));
    }

    @Transactional
    public Festival createFestival(Festival festival) {
        return festivalRepository.save(festival);
    }

    @Transactional
    public void deleteFestival(Long festivalId) {
        festivalRepository.deleteById(festivalId);
    }
}
