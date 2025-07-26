package com.swyp10.domain.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyReviewListResponse {
    @Schema(description = "리뷰 총 개수", example = "5")
    private Long totalCount;

    @Schema(description = "리뷰 목록")
    private List<MyReviewResponse> reviews;
}