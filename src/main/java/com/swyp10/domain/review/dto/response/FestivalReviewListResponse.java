package com.swyp10.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FestivalReviewListResponse {
    @Schema(description = "리뷰 총 개수", example = "12")
    private Long totalCount;
    @Schema(description = "리뷰 목록")
    private List<FestivalReviewResponse> reviews;
}
