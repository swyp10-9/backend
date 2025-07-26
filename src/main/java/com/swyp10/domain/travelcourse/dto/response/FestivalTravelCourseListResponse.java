package com.swyp10.domain.travelcourse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FestivalTravelCourseListResponse {
    @Schema(description = "여행 코스 목록")
    private List<FestivalTravelCourseResponse> courses;
}