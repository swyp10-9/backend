package com.swyp10.domain.mypage.controller;

import com.swyp10.domain.mypage.dto.request.MyInfoUpdateRequest;
import com.swyp10.domain.mypage.dto.response.MyReviewListResponse;
import com.swyp10.domain.mypage.dto.response.MyInfoResponse;
import com.swyp10.domain.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@Tag(name = "마이페이지", description = "마이페이지 API")
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "리뷰 목록 조회", description = "사용자 리뷰 작성 목록 조회")
    @GetMapping("/reviews")
    public MyReviewListResponse getMyReviews(@AuthenticationPrincipal Long userId) {
        return myPageService.getMyReviews(userId);
    }

    @Operation(summary = "리뷰 삭제", description = "사용자 리뷰 삭제")
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteMyReview(@PathVariable Long reviewId, @AuthenticationPrincipal Long userId) {
        myPageService.deleteMyReview(userId, reviewId);
    }

    @Operation(summary = "북마크 취소", description = "사용자 북마크 취소")
    @DeleteMapping("/bookmarks/{festivalId}")
    public void cancelBookmark(@PathVariable Long festivalId, @AuthenticationPrincipal Long userId) {
        myPageService.cancelBookmark(userId, festivalId);
    }

    @Operation(summary = "사용자 정보 변경", description = "사용자 닉네임 변경")
    @PatchMapping("/me")
    public MyInfoResponse updateMyInfo(@ModelAttribute @Valid MyInfoUpdateRequest request, @AuthenticationPrincipal Long userId) {
        return myPageService.updateMyInfo(userId, request);
    }
}
