package com.swyp10.domain.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MyReviewResponse {
    @Schema(description = "리뷰 ID", required = true, nullable = false, example = "11")
    private Long id;

    @Schema(description = "축제 ID", required = true, nullable = false, example = "123")
    private Long festivalId;

    @Schema(description = "축제명", required = true, nullable = false, example = "부산 불꽃축제")
    private String festivalTitle;

    @Schema(description = "썸네일 이미지 URL", required = false, nullable = true, example = "https://...")
    private String festivalThumbnail;

    @Schema(description = "리뷰 내용", required = true, nullable = false, example = "정말 재미있었어요!")
    private String content;

    @Schema(description = "리뷰 작성 날짜", required = true, nullable = false, example = "2025.08.01")
    private LocalDate createdAt;
}
