package com.swyp10.domain.review.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FestivalReviewResponse {
    @Schema(description = "리뷰 ID", example = "201")
    private Long id;
    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String nickname;
    @Schema(description = "작성자 프로필 이미지 URL", example = "https://...")
    private String profileImage;
    @Schema(description = "리뷰 내용", example = "정말 재밌었어요!")
    private String content;
    @Schema(description = "작성일", example = "2025-08-01")
    private LocalDate createdAt;
}