package com.swyp10.domain.mypage.service;

import com.swyp10.domain.mypage.dto.request.MyInfoUpdateRequest;
import com.swyp10.domain.mypage.dto.response.MyReviewListResponse;
import com.swyp10.domain.mypage.dto.response.MyInfoResponse;

public interface MyPageService {
    MyReviewListResponse getMyReviews(Long userId);
    void deleteMyReview(Long userId, Long reviewId);
    void cancelBookmark(Long userId, Long festivalId);
    MyInfoResponse updateMyInfo(Long userId, MyInfoUpdateRequest request);
}
