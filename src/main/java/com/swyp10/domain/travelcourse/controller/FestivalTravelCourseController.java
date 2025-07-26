package com.swyp10.domain.travelcourse.controller;

import com.swyp10.domain.travelcourse.dto.response.FestivalTravelCourseListResponse;
import com.swyp10.domain.travelcourse.service.TravelCourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "여행 코스", description = "여행 코스 조회 API")
public class FestivalTravelCourseController {

    private final TravelCourseService travelCourseService;

    @GetMapping("/{festivalId}/travel-courses")
    public FestivalTravelCourseListResponse getFestivalTravelCourses(@PathVariable Long festivalId) {
        return travelCourseService.getFestivalTravelCourses(festivalId);
    }
}
