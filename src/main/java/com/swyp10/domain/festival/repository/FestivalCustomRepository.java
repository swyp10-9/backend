package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.dto.request.FestivalMapRequest;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FestivalCustomRepository {
    Page<FestivalSummaryResponse> findFestivalsForMap(FestivalMapRequest request, Pageable pageable);
}
