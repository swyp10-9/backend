package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.dto.request.FestivalCalendarRequest;
import com.swyp10.domain.festival.dto.request.FestivalMapRequest;
import com.swyp10.domain.festival.dto.response.FestivalDailyCountResponse;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface FestivalCustomRepository {
    Page<FestivalSummaryResponse> findFestivalsForMap(FestivalMapRequest request, Pageable pageable);
    Page<FestivalSummaryResponse> findFestivalsForCalendar(FestivalCalendarRequest request, Pageable pageable);
    List<FestivalDailyCountResponse.DailyCount> findDailyFestivalCounts(LocalDate startDate, LocalDate endDate);
}
