package com.swyp10.domain.mypage.controller;

import com.swyp10.domain.mypage.dto.request.MyInfoUpdateRequest;
import com.swyp10.domain.mypage.dto.response.MyReviewListResponse;
import com.swyp10.domain.mypage.dto.response.MyInfoResponse;
import com.swyp10.domain.mypage.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/reviews")
    public MyReviewListResponse getMyReviews(@AuthenticationPrincipal Long userId) {
        return myPageService.getMyReviews(userId);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public void deleteMyReview(@PathVariable Long reviewId, @AuthenticationPrincipal Long userId) {
        myPageService.deleteMyReview(userId, reviewId);
    }

    @DeleteMapping("/bookmarks/{festivalId}")
    public void cancelBookmark(@PathVariable Long festivalId, @AuthenticationPrincipal Long userId) {
        myPageService.cancelBookmark(userId, festivalId);
    }

    @PatchMapping("/me")
    public MyInfoResponse updateMyInfo(@ModelAttribute @Valid MyInfoUpdateRequest request, @AuthenticationPrincipal Long userId) {
        return myPageService.updateMyInfo(userId, request);
    }
}
