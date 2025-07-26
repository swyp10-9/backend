package com.swyp10.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class FestivalReviewCreateRequest {
    @Schema(description = "리뷰 내용", example = "정말 즐거운 축제였어요!")
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 500, message = "리뷰 내용은 10자 이상 500자 이하로 입력해야 합니다.")
    private String content;
}
