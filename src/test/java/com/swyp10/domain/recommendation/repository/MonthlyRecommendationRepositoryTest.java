package com.swyp10.domain.recommendation.repository;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.recommendation.entity.MonthlyRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MonthlyRecommendationRepository 테스트")
class MonthlyRecommendationRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private MonthlyRecommendationRepository recommendationRepository;

    private Festival festival;

    @BeforeEach
    void setUp() {
        festival = Festival.builder()
            .name("봄꽃축제")
            .startDate(LocalDate.of(2025, 3, 20))
            .endDate(LocalDate.of(2025, 3, 25))
            .description("화려한 봄꽃이 가득한 축제")
            .theme(null)
            .status(null)
            .thumbnail("https://image.url")
            .build();
        entityManager.persist(festival);
    }

    @Test
    @DisplayName("월간 추천 저장 및 조회")
    void save_and_find() {
        // given
        MonthlyRecommendation recommendation = MonthlyRecommendation.builder()
            .festival(festival)
            .sortSq(1L)
            .build();

        MonthlyRecommendation saved = recommendationRepository.save(recommendation);
        entityManager.flush();

        // when
        Optional<MonthlyRecommendation> found = recommendationRepository.findById(festival.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getFestival().getName()).isEqualTo("봄꽃축제");
        assertThat(found.get().getSortSq()).isEqualTo(1L);
    }

    @Test
    @DisplayName("월간 추천 삭제")
    void delete() {
        // given
        MonthlyRecommendation recommendation = MonthlyRecommendation.builder()
            .festival(festival)
            .sortSq(2L)
            .build();

        recommendationRepository.save(recommendation);
        entityManager.flush();

        // when
        recommendationRepository.deleteById(festival.getId());
        entityManager.flush();

        // then
        assertThat(recommendationRepository.findById(festival.getId())).isNotPresent();
    }
}
