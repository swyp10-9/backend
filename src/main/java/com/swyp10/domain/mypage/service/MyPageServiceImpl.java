package com.swyp10.domain.mypage.service;

import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.domain.mypage.dto.request.MyInfoUpdateRequest;
import com.swyp10.domain.mypage.dto.response.MyInfoResponse;
import com.swyp10.domain.mypage.dto.response.MyReviewListResponse;
import com.swyp10.domain.review.repository.UserReviewRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.global.page.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private final UserReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final UserBookmarkRepository bookmarkRepository;

    @Override
    public MyReviewListResponse getMyReviews(Long userId, PageRequest pageRequest) {
        return null;
    }

    @Override
    @Transactional
    public void deleteMyReview(Long userId, Long reviewId) {
        var review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "리뷰를 찾을 수 없습니다."));
        if (!review.getUser().getUserId().equals(userId)) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "내가 쓴 리뷰만 삭제할 수 있습니다.");
        }
        reviewRepository.deleteById(reviewId);
    }

    @Override
    @Transactional
    public void cancelBookmark(Long userId, Long festivalId) {
        UserBookmark bookmark = bookmarkRepository.findByUser_UserIdAndFestival_ContentId(userId, String.valueOf(festivalId))
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "해당 북마크가 없습니다."));
        bookmarkRepository.delete(bookmark);
    }

    @Override
    @Transactional
    public MyInfoResponse updateMyInfo(Long userId, MyInfoUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "유저를 찾을 수 없습니다."));
        user.updateProfile(request.getNickname());

        return MyInfoResponse.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .profileImage(user.getProfileImage())
            .build();
    }
}
