package com.swyp10.domain.festival.controller;

import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @GetMapping("/map")
    public FestivalListResponse getFestivalsForMapCalendar(@ModelAttribute FestivalMapRequest request) {
        return festivalService.getFestivalsForMap(request);
    }

    @GetMapping("/calendar")
    public FestivalListResponse getFestivalsForMapCalendar(@ModelAttribute FestivalCalendarRequest request) {
        return festivalService.getFestivalsForCalendar(request);
    }

    @GetMapping("/personal-test")
    public FestivalListResponse getFestivalsForPersonalTest(@ModelAttribute FestivalPersonalTestRequest request) {
        return festivalService.getFestivalsForPersonalTest(request);
    }

    @GetMapping("/search")
    public FestivalListResponse searchFestivals(@ModelAttribute FestivalSearchRequest request) {
        return festivalService.searchFestivals(request);
    }

    @GetMapping("/mypage")
    public FestivalListResponse getMyPageFestivals(@ModelAttribute FestivalMyPageRequest request) {
        return festivalService.getMyPageFestivals(request);
    }
}
