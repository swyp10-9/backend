package com.swyp10.domain.review.service;

import com.swyp10.domain.review.entity.ReviewLike;
import com.swyp10.domain.review.repository.ReviewLikeRepository;
import com.swyp10.global.exception.ApplicationException;
import com.swyp10.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;

    public ReviewLike getReviewLike(Long reviewLikeId) {
        return reviewLikeRepository.findById(reviewLikeId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "ReviewLike not found: " + reviewLikeId));
    }

    @Transactional
    public ReviewLike createReviewLike(ReviewLike reviewLike) {
        return reviewLikeRepository.save(reviewLike);
    }

    @Transactional
    public void deleteReviewLike(Long reviewLikeId) {
        reviewLikeRepository.deleteById(reviewLikeId);
    }
}
