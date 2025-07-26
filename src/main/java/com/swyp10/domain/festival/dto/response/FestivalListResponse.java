package com.swyp10.domain.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FestivalListResponse {
    @Schema(description = "총 조회 개수", example = "1024")
    private Long totalCount;

    @Schema(description = "페이지 오프셋", example = "0")
    private Integer offset;

    @Schema(description = "페이지 사이즈", example = "20")
    private Integer limit;

    @Schema(description = "축제 목록")
    private List<FestivalSummaryResponse> festivals;
}
