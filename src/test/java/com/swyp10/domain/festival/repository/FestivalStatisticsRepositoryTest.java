package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FestivalStatisticsRepository 테스트")
class FestivalStatisticsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FestivalStatisticsRepository statisticsRepository;

    private Festival testFestival;

    @BeforeEach
    void setUp() {
        testFestival = Festival.builder()
            .name("광안리 불꽃축제")
            .startDate(LocalDate.of(2025, 10, 5))
            .endDate(LocalDate.of(2025, 10, 6))
            .build();

        entityManager.persist(testFestival);
        entityManager.flush();
    }

    @Test
    @DisplayName("통계 저장 및 조회")
    void save_and_find_statistics() {
        // given
        FestivalStatistics stats = FestivalStatistics.builder()
            .festival(testFestival)
            .regionCode(26)
            .viewCount(100)
            .bookmarkCount(50)
            .ratingAvg(BigDecimal.valueOf(4.5))
            .ratingCount(10)
            .build();

        // when
        FestivalStatistics saved = statisticsRepository.save(stats);
        entityManager.flush();

        Optional<FestivalStatistics> found = statisticsRepository.findById(saved.getFestivalId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getViewCount()).isEqualTo(100);
        assertThat(found.get().getRatingAvg()).isEqualTo("4.5");
    }

    @Test
    @DisplayName("통계 삭제")
    void delete_statistics() {
        // given
        FestivalStatistics stats = FestivalStatistics.builder()
            .festival(testFestival)
            .regionCode(26)
            .viewCount(10)
            .bookmarkCount(5)
            .ratingAvg(BigDecimal.valueOf(3.8))
            .ratingCount(2)
            .build();

        // when
        FestivalStatistics saved = statisticsRepository.save(stats);
        Long id = saved.getFestivalId();
        entityManager.flush();

        statisticsRepository.deleteById(id);
        entityManager.flush();

        // then
        assertThat(statisticsRepository.findById(id)).isNotPresent();
    }
}
