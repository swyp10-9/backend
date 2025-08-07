package com.swyp10.domain.festival.controller;

import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.service.FestivalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "축제", description = "축제 조회 API")
public class FestivalController {

    private final FestivalService festivalService;

    @Operation(summary = "축제 리스트 조회 - 지도 페이지", description = "축제 리스트 조회 - 지도 페이지")
    @GetMapping("/map")
    public FestivalListResponse getFestivalsForMap(@ModelAttribute @ParameterObject FestivalMapRequest request) {
        return festivalService.getFestivalsForMap(request);
    }

    @Operation(summary = "축제 리스트 조회 - 달력 페이지", description = "축제 리스트 조회 - 달력 페이지")
    @GetMapping("/calendar")
    public FestivalListResponse getFestivalsForCalendar(@ModelAttribute @ParameterObject FestivalCalendarRequest request) {
        return festivalService.getFestivalsForCalendar(request);
    }

    @Operation(summary = "축제 리스트 조회 - 맞춤 축제 페이지", description = "축제 리스트 조회 - 맞춤 축제 페이지")
    @GetMapping("/personal-test")
    public FestivalListResponse getFestivalsForPersonalTest(@ModelAttribute @ParameterObject FestivalPersonalTestRequest request) {
        return festivalService.getFestivalsForPersonalTest(request);
    }

    @Operation(summary = "축제 리스트 조회 - 검색 페이지", description = "축제 리스트 조회 - 검색 페이지")
    @GetMapping("/search")
    public FestivalListResponse searchFestivals(@ModelAttribute @ParameterObject FestivalSearchRequest request) {
        return festivalService.searchFestivals(request);
    }

    @Operation(summary = "축제 리스트 조회 - 마이페이지", description = "축제 리스트 조회 - 마이페이지")
    @GetMapping("/mypage")
    public FestivalListResponse getMyPageFestivals(@ModelAttribute @ParameterObject FestivalMyPageRequest request) {
        return festivalService.getMyPageFestivals(request);
    }
}
