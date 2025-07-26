package com.swyp10.domain.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
class MyReviewResponse {
    @Schema(description = "리뷰 ID", example = "11")
    private Long id;

    @Schema(description = "축제 ID", example = "123")
    private Long festivalId;

    @Schema(description = "축제명", example = "부산 불꽃축제")
    private String festivalTitle;

    @Schema(description = "썸네일 이미지 URL", example = "https://...")
    private String festivalThumbnail;

    @Schema(description = "리뷰 내용", example = "정말 재미있었어요!")
    private String content;

    @Schema(description = "리뷰 작성 날짜", example = "2025.08.01")
    private LocalDate createdAt;
}
