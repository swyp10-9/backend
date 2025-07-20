package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.entity.Festival;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FestivalRepository 테스트")
class FestivalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FestivalRepository festivalRepository;

    private Festival testFestival;

    @BeforeEach
    void setUp() {
        testFestival = Festival.builder()
            .name("서울 불꽃축제")
            .startDate(LocalDate.of(2025, 10, 1))
            .endDate(LocalDate.of(2025, 10, 2))
            .build();
    }

    @Test
    @DisplayName("축제 저장 및 조회")
    void saveAndFindById() {
        // given
        Festival saved = festivalRepository.save(testFestival);
        entityManager.flush();

        // when
        Festival found = festivalRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getName()).isEqualTo("서울 불꽃축제");
    }

    @Test
    @DisplayName("축제 삭제")
    void deleteFestival() {
        // given
        Festival saved = festivalRepository.save(testFestival);
        entityManager.flush();

        // when
        festivalRepository.deleteById(saved.getId());

        // then
        assertThat(festivalRepository.findById(saved.getId())).isEmpty();
    }
}