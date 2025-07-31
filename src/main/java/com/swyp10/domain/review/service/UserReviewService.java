package com.swyp10.domain.review.service;

import com.swyp10.domain.review.dto.request.FestivalReviewCreateRequest;
import com.swyp10.domain.review.dto.response.FestivalReviewListResponse;
import com.swyp10.domain.review.entity.UserReview;
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
public class UserReviewService {

    private final UserReviewRepository userReviewRepository;

    public UserReview getUserReview(Long userReviewId) {
        return userReviewRepository.findById(userReviewId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "UserReview not found: " + userReviewId));
    }

    @Transactional
    public UserReview createUserReview(UserReview userReview) {
        return userReviewRepository.save(userReview);
    }

    @Transactional
    public void deleteUserReview(Long userReviewId) {
        userReviewRepository.deleteById(userReviewId);
    }

    public FestivalReviewListResponse getFestivalReviews(Long festivalId, PageRequest pageRequest) {
        return null;
    }

    @Transactional
    public Long createFestivalReview(Long userId, Long festivalId, FestivalReviewCreateRequest request) {
        return null;
    }

}
