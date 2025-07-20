package com.swyp10.domain.review.repository;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.review.entity.ReviewLike;
import com.swyp10.domain.review.entity.UserReview;
import com.swyp10.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ReviewLikeRepository 테스트")
class ReviewLikeRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    private ReviewLike reviewLike;

    @BeforeEach
    void setUp() {
        User user = em.persist(User.builder()
            .email("liker@example.com")
            .password("pw")
            .nickname("좋아요유저")
            .signupCompleted(true)
            .build());

        Festival festival = em.persist(Festival.builder()
            .name("봄꽃축제")
            .startDate(java.time.LocalDate.now())
            .endDate(java.time.LocalDate.now().plusDays(3))
            .theme(null)
            .description("봄꽃이 활짝 핀 축제입니다.")
            .thumbnail("http://example.com/image.jpg")
            .build());

        UserReview review = em.persist(UserReview.builder()
            .festival(festival)
            .content("축제가 너무 좋아요")
            .rating(5)
            .user(user)
            .build());

        reviewLike = ReviewLike.builder()
            .user(user)
            .userReview(review)
            .build();
    }

    @Test
    @DisplayName("ReviewLike 저장 및 조회")
    void saveAndFindById() {
        // given
        ReviewLike saved = reviewLikeRepository.save(reviewLike);
        em.flush();

        // when
        Optional<ReviewLike> found = reviewLikeRepository.findById(saved.getReviewLikeId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserReview()).isEqualTo(reviewLike.getUserReview());
    }

    @Test
    @DisplayName("ReviewLike 삭제")
    void deleteReviewLike() {
        // given
        ReviewLike saved = reviewLikeRepository.save(reviewLike);
        em.flush();

        // when
        reviewLikeRepository.deleteById(saved.getReviewLikeId());
        em.flush();

        // then
        assertThat(reviewLikeRepository.findById(saved.getReviewLikeId())).isNotPresent();
    }
}
