package com.swyp10.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class FestivalReviewCreateRequest {
    @Schema(description = "리뷰 내용", example = "정말 즐거운 축제였어요!")
    @NotBlank
    @Size(max = 500)
    private String content;
}
