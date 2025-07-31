package com.swyp10.domain.travelcourse.dto.request;

import com.swyp10.global.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "축제 여행코스 목록 조회 요청")
public class FestivalTravelCoursePageRequest extends PageRequest {
    
    @Schema(description = "축제 ID", example = "1", required = true)
    private Long festivalId;
    
    @Schema(description = "코스 타입", example = "당일코스")
    private String courseType;
    
    @Schema(description = "테마", example = "가족여행")
    private String theme;
    
    @Schema(description = "정렬 기준 (popularity,desc | duration,asc)", example = "popularity,desc")
    private String sort;
}
