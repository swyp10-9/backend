package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalDetailResponse;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.entity.Festival;

public interface FestivalService {
    Festival getFestival(Long festivalId);
    Festival createFestival(Festival festival);
    void deleteFestival(Long festivalId);

    FestivalListResponse getFestivalsForMap(FestivalMapRequest request);
    FestivalListResponse getFestivalsForCalendar(FestivalCalendarRequest request);
    FestivalListResponse getFestivalsForPersonalTest(FestivalPersonalTestRequest request);
    FestivalListResponse searchFestivals(FestivalSearchRequest request);
    FestivalListResponse getMyPageFestivals(FestivalMyPageRequest request);
    FestivalDetailResponse getFestivalDetail(Long festivalId);
}
