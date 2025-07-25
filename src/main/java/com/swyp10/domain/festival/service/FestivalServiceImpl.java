package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalDetailResponse;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalServiceImpl implements FestivalService {

    private final FestivalRepository festivalRepository;

    @Override
    public Festival getFestival(Long festivalId) {
        return festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "Festival not found: " + festivalId));
    }

    @Override
    @Transactional
    public Festival createFestival(Festival festival) {
        return festivalRepository.save(festival);
    }

    @Override
    @Transactional
    public void deleteFestival(Long festivalId) {
        festivalRepository.deleteById(festivalId);
    }

    @Override
    public FestivalListResponse getFestivalsForMap(FestivalMapRequest request) {
        return null;
    }

    @Override
    public FestivalListResponse getFestivalsForCalendar(FestivalCalendarRequest request) {
        return null;
    }

    @Override
    public FestivalListResponse getFestivalsForPersonalTest(FestivalPersonalTestRequest request) {
        return null;
    }

    @Override
    public FestivalListResponse searchFestivals(FestivalSearchRequest request) {
        return null;
    }

    @Override
    public FestivalListResponse getMyPageFestivals(FestivalMyPageRequest request) {
        return null;
    }

    @Override
    public FestivalDetailResponse getFestivalDetail(Long festivalId) {
        return null;
    }
}
