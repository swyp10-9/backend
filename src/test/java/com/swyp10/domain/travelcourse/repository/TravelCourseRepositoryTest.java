package com.swyp10.domain.travelcourse.repository;

import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.entity.TravelDifficulty;
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
@DisplayName("TravelCourseRepository 테스트")
class TravelCourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TravelCourseRepository travelCourseRepository;

    private TravelCourse travelCourse;

    @BeforeEach
    void setUp() {
        travelCourse = TravelCourse.builder()
            .title("한강 산책 코스")
            .durationHours(2)
            .difficultyLevel(TravelDifficulty.EASY)
            .build();
    }

    @Test
    @DisplayName("TravelCourse 저장 및 조회")
    void saveAndFindById() {
        // given
        TravelCourse saved = travelCourseRepository.save(travelCourse);
        entityManager.flush();

        // when
        Optional<TravelCourse> found = travelCourseRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("한강 산책 코스");
    }

    @Test
    @DisplayName("TravelCourse 삭제")
    void deleteTravelCourse() {
        // given
        TravelCourse saved = travelCourseRepository.save(travelCourse);
        Long id = saved.getId();
        entityManager.flush();

        // when
        travelCourseRepository.deleteById(id);
        entityManager.flush();

        // then
        assertThat(travelCourseRepository.findById(id)).isNotPresent();
    }
}
