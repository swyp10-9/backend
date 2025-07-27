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

    @Schema(description = "근처 볼거리 목록")
    private List<NearbyAttractionResponse> nearbyAttractions;

    @Getter
    @Builder
    public static class NearbyAttractionResponse {
        @Schema(description = "장소명", example = "해운대 해수욕장")
        private String name;

        @Schema(description = "썸네일 이미지 URL", example = "https://...")
        private String thumbnail;

        @Schema(description = "경도", example = "127.5881015063")
        private String mapx;

        @Schema(description = "위도", example = "36.9913818048")
        private String mapy;

        @Schema(description = "장소 설명/상세 페이지 URL", example = "https://visitbusan.net/haeundae")
        private String descriptionUrl;
    }
}