package com.swyp10.domain.review.controller;

import com.swyp10.config.security.OptionalUserId;
import com.swyp10.domain.review.dto.request.FestivalReviewCreateRequest;
import com.swyp10.domain.review.dto.response.FestivalReviewListResponse;
import com.swyp10.domain.review.service.UserReviewService;
import com.swyp10.global.page.PageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "리뷰", description = "리뷰 조회 & 작성 API")
public class FestivalReviewController {

    private final UserReviewService reviewService;

    @Operation(summary = "축제 리뷰 목록 조회", description = "해당 축제의 리뷰 목록 조회 (페이징 지원)")
    @GetMapping("/{festivalId}/reviews")
    public FestivalReviewListResponse getFestivalReviews(
            @PathVariable Long festivalId,
            @ModelAttribute @ParameterObject PageRequest pageRequest) {
        return reviewService.getFestivalReviews(festivalId, pageRequest);
    }

    @Operation(summary = "리뷰 등록",
        description = "사용자가 특정 축제에 리뷰 작성",
        security = { @SecurityRequirement(name = "Bearer Authentication") }
    )
    @PostMapping("/{festivalId}/reviews")
    public Long createFestivalReview(
        @PathVariable Long festivalId,
        @Parameter(hidden = true) @OptionalUserId Long userId,
        @RequestBody @Valid FestivalReviewCreateRequest request
    ) {
        return reviewService.createFestivalReview(userId, festivalId, request);
    }
}
