package com.swyp10.domain.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FestivalDetailResponse {
    @Schema(description = "축제 ID", example = "1001")
    private Long id;
    @Schema(description = "축제명", example = "부산 불꽃축제")
    private String title;
    @Schema(description = "주소", example = "부산광역시 해운대구")
    private String address;
    @Schema(description = "축제 시작일", example = "2025-09-01")
    private String startDate;
    @Schema(description = "축제 종료일", example = "2025-09-03")
    private String endDate;
    @Schema(description = "상세 설명", example = "바다와 함께하는 불꽃놀이 축제")
    private String description;
    @Schema(description = "대표 이미지", example = "https://...")
    private String imageUrl;
}
