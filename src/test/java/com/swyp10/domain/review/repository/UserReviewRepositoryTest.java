package com.swyp10.domain.review.repository;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.region.entity.Region;
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
@DisplayName("UserReviewRepository 테스트")
class UserReviewRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserReviewRepository userReviewRepository;

    private UserReview userReview;

    @BeforeEach
    void setUp() {
        User user = em.persist(User.builder()
            .email("reviewer@example.com")
            .password("pw")
            .nickname("리뷰어")
            .signupCompleted(true)
            .build());

        Region region = em.persist(Region.builder()
            .regionCode(1)
            .regionName("서울")
            .build());

        Festival festival = em.persist(Festival.builder()
            .name("봄꽃축제")
            .region(region)
            .startDate(java.time.LocalDate.now())
            .endDate(java.time.LocalDate.now().plusDays(3))
            .theme(null)
            .description("봄꽃이 활짝 핀 축제입니다.")
            .thumbnail("http://example.com/image.jpg")
            .build());

        userReview = UserReview.builder()
            .user(user)
            .festival(festival)
            .rating(5)
            .content("정말 재밌었어요!")
            .build();
    }

    @Test
    @DisplayName("UserReview 저장 및 조회")
    void saveAndFindById() {
        // given
        UserReview saved = userReviewRepository.save(userReview);
        em.flush();

        // when
        Optional<UserReview> found = userReviewRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).contains("재밌었어요");
    }

    @Test
    @DisplayName("UserReview 삭제")
    void deleteUserReview() {
        // given
        UserReview saved = userReviewRepository.save(userReview);
        em.flush();

        // when
        userReviewRepository.deleteById(saved.getId());
        em.flush();

        // then
        assertThat(userReviewRepository.findById(saved.getId())).isNotPresent();
    }
}
