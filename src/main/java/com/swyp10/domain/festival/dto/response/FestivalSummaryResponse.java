package com.swyp10.domain.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FestivalSummaryResponse {
    @Schema(description = "축제 ID", required = false, nullable = false, example = "1234")
    private Long id;

    @Schema(description = "썸네일 이미지 URL", required = false, nullable = true, example = "https://...")
    private String thumbnail;

    @Schema(description = "테마", required = false, nullable = true, example = "음식/미식")
    private String theme;

    @Schema(description = "축제명", required = false, nullable = false, example = "부산 불꽃축제")
    private String title;

    @Schema(description = "북마크 여부", required = false, nullable = false, example = "true")
    private Boolean bookmarked;

    @Schema(description = "주소", required = false, nullable = true, example = "부산광역시 해운대구")
    private String address;

    @Schema(description = "축제 시작일", required = false, nullable = true, example = "2025-09-01", type = "string", format = "date")
    private LocalDate startDate;

    @Schema(description = "축제 종료일", required = false, nullable = true, example = "2025-09-03", type = "string", format = "date")
    private LocalDate endDate;
}
