package com.swyp10.domain.review.repository;

import com.swyp10.domain.review.entity.UserReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReviewRepository extends JpaRepository<UserReview, Long> {
    Page<UserReview> findByFestival_ContentIdOrderByCreatedAtDesc(String contentId, Pageable pageable);
    boolean existsByUser_UserIdAndFestival_ContentId(Long userId, String contentId);

    // 마이페이지: 내 리뷰 목록(최신순)
    Page<UserReview> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 소유자 검증용
    Optional<UserReview> findByUser_UserIdAndUserReviewId(Long userId, Long userReviewId);

    boolean existsByUser_UserIdAndUserReviewId(Long userId, Long userReviewId);
}
