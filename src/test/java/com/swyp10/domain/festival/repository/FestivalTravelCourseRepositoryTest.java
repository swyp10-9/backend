package com.swyp10.domain.festival.repository;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalTravelCourse;
import com.swyp10.domain.festival.entity.FestivalTravelCourseId;
import com.swyp10.domain.travelcourse.entity.TravelCourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FestivalTravelCourseRepository 테스트")
class FestivalTravelCourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FestivalTravelCourseRepository repository;

    private Festival festival;
    private TravelCourse travelCourse;

    @BeforeEach
    void setUp() {
        festival = Festival.builder()
            .name("부산불꽃축제")
            .startDate(LocalDate.of(2025, 10, 10))
            .endDate(LocalDate.of(2025, 10, 11))
            .build();

        travelCourse = TravelCourse.builder()
            .title("광안리 해변 산책코스")
            .durationHours(2)
            .difficultyLevel(null)  // enum을 사용 중이면 실제 값으로 대체
            .build();

        entityManager.persist(festival);
        entityManager.persist(travelCourse);
        entityManager.flush();
    }

    @Test
    @DisplayName("축제-코스 매핑 저장 및 조회")
    void saveAndFindByFestivalId() {
        FestivalTravelCourseId id = new FestivalTravelCourseId(festival.getId(), travelCourse.getId());
        FestivalTravelCourse mapping = FestivalTravelCourse.builder()
            .id(id)
            .festival(festival)
            .travelCourse(travelCourse)
            .build();

        repository.save(mapping);
        entityManager.flush();

        List<FestivalTravelCourse> result = repository.findByFestival_Id(festival.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFestival().getId()).isEqualTo(festival.getId());
        assertThat(result.get(0).getTravelCourse().getId()).isEqualTo(travelCourse.getId());
    }

    @Test
    @DisplayName("축제-코스 매핑 삭제")
    void deleteByFestivalIdAndTravelCourseId() {
        FestivalTravelCourseId id = new FestivalTravelCourseId(festival.getId(), travelCourse.getId());
        FestivalTravelCourse mapping = FestivalTravelCourse.builder()
            .id(id)
            .festival(festival)
            .travelCourse(travelCourse)
            .build();

        repository.save(mapping);
        entityManager.flush();

        repository.deleteByFestival_IdAndTravelCourse_Id(festival.getId(), travelCourse.getId());
        entityManager.flush();

        Optional<FestivalTravelCourse> result = repository.findById(id);
        assertThat(result).isNotPresent();
    }
}
