package com.swyp10.domain.review.repository;

import com.swyp10.domain.review.entity.UserReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReviewRepository extends JpaRepository<UserReview, Long> {
}
