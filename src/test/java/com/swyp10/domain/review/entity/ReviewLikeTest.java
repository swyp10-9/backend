package com.swyp10.domain.review.entity;

import com.swyp10.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewLike Entity 테스트")
class ReviewLikeTest {

    @Test
    @DisplayName("ReviewLike 엔티티 생성")
    void createReviewLike() {
        UserReview review = UserReview.builder()
            .content("맛있어요!")
            .rating(5)
            .build();

        User user = User.builder()
            .userId(1L)
            .nickname("좋아요왕")
            .email("like@test.com")
            .build();

        ReviewLike like = ReviewLike.builder()
            .userReview(review)
            .user(user)
            .build();

        like.onCreate(); // PrePersist 수동 실행

        assertThat(like.getUser()).isEqualTo(user);
        assertThat(like.getUserReview()).isEqualTo(review);
        assertThat(like.getCreatedAt()).isNotNull();
        assertThat(like.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("ReviewLike 엔티티 soft delete")
    void softDeleteLike() {
        ReviewLike like = ReviewLike.builder().build();

        assertThat(like.getDeletedAt()).isNull();

        like.markDeleted();

        assertThat(like.getDeletedAt()).isNotNull();
        assertThat(like.isDeleted()).isTrue();
    }
}
