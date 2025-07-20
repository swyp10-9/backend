package com.swyp10.domain.review.entity;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalTheme;
import com.swyp10.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserReview Entity 테스트")
class UserReviewTest {

    @Test
    @DisplayName("UserReview 엔티티 생성")
    void createReview() {
        Festival festival = Festival.builder()
            .name("춘천 닭갈비 축제")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(2))
            .theme(FestivalTheme.FOOD)
            .build();

        User user = User.builder()
            .userId(1L)
            .email("user@test.com")
            .nickname("리뷰러")
            .build();

        UserReview review = UserReview.builder()
            .content("정말 맛있고 재밌었어요!")
            .rating(5)
            .festival(festival)
            .user(user)
            .build();

        assertThat(review.getContent()).contains("맛있고");
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getFestival()).isEqualTo(festival);
        assertThat(review.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("UserReview 엔티티 수정")
    void updateReview() {
        UserReview review = UserReview.builder()
            .content("초기 내용")
            .rating(2)
            .build();

        review.updateContent("수정된 내용입니다.", 4);

        assertThat(review.getContent()).isEqualTo("수정된 내용입니다.");
        assertThat(review.getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("UserReview - ReviewLike 연관 관계 메서드")
    void addReviewLike() {
        UserReview review = UserReview.builder()
            .content("좋아요 테스트용")
            .rating(3)
            .build();

        ReviewLike like = new ReviewLike();
        review.addReviewLike(like);

        assertThat(review.getReviewLikes()).contains(like);
        assertThat(like.getUserReview()).isEqualTo(review);
    }
}
