package com.swyp10.domain.travelcourse.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FestivalTravelCourseResponse {
    @Schema(description = "코스 ID", required = false, nullable = false, example = "22")
    private Long id;
    @Schema(description = "코스명", required = false, nullable = false, example = "해운대 산책로")
    private String title;
    @Schema(description = "시각", required = false, nullable = true, example = "11:00")
    private String time;
}