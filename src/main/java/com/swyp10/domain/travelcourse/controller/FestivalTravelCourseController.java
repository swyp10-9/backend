package com.swyp10.domain.travelcourse.controller;

import com.swyp10.domain.travelcourse.dto.request.FestivalTravelCoursePageRequest;
import com.swyp10.domain.travelcourse.dto.response.FestivalTravelCourseListResponse;
import com.swyp10.domain.travelcourse.service.TravelCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "여행 코스", description = "여행 코스 조회 API")
public class FestivalTravelCourseController {

    private final TravelCourseService travelCourseService;

    @Operation(summary = "여행 코스 조회", description = "축제 지역 내 여행 코스 추천 (페이징 지원)")
    @GetMapping("/{festivalId}/travel-courses")
    public FestivalTravelCourseListResponse getFestivalTravelCourses(
            @PathVariable Long festivalId,
            @ModelAttribute @ParameterObject FestivalTravelCoursePageRequest request) {
        request.setFestivalId(festivalId);
        return travelCourseService.getFestivalTravelCourses(request);
    }
}
