package com.swyp10.domain.festival.controller;

import com.swyp10.domain.festival.dto.response.FestivalDetailResponse;
import com.swyp10.domain.festival.service.FestivalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "축제상세", description = "축제 상세 조회 API")
public class FestivalDetailController {

    private final FestivalService festivalService;

    @Operation(summary = "축제 상세 조회", description = "축제 상세 조회")
    @GetMapping("/{festivalId}")
    public FestivalDetailResponse getFestivalDetail(@PathVariable Long festivalId) {
        return festivalService.getFestivalDetail(festivalId);
    }
}
