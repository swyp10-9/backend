package com.swyp10.domain.festival.controller;

import com.swyp10.domain.festival.dto.response.FestivalDailyCountResponse;
import com.swyp10.domain.festival.service.FestivalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "축제 통계", description = "축제 일자별 통계 API")
public class FestivalCalendarStatController {

    private final FestivalService festivalService;

    @Operation(
        summary = "달력 월별 일자별 축제 개수 조회",
        description = "특정 월의 각 날짜별 진행 중인 축제 개수 조회"
    )
    @GetMapping("/calendar/daily-count")
    public FestivalDailyCountResponse getDailyFestivalCount(
        @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return festivalService.getDailyFestivalCount(startDate, endDate);
    }
}
