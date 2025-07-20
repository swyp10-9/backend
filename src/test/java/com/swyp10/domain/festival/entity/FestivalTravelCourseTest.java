package com.swyp10.domain.festival.entity;

import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.entity.TravelDifficulty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FestivalTravelCourse Entity 테스트")
class FestivalTravelCourseTest {

    @Test
    @DisplayName("FestivalTravelCourse 엔티티 생성")
    void createFestivalTravelCourse() {
        Festival festival = Festival.builder()
            .id(1L)
            .name("부산 불꽃축제")
            .startDate(LocalDate.of(2025, 10, 1))
            .endDate(LocalDate.of(2025, 10, 5))
            .build();

        TravelCourse course = TravelCourse.builder()
            .id(101L)
            .title("광안리 해변 산책")
            .durationHours(2)
            .difficultyLevel(TravelDifficulty.EASY)
            .build();

        FestivalTravelCourseId id = new FestivalTravelCourseId(festival.getId(), course.getId());

        FestivalTravelCourse mapping = FestivalTravelCourse.builder()
            .id(id)
            .festival(festival)
            .travelCourse(course)
            .build();

        mapping.onCreate(); // @PrePersist 수동 호출

        assertThat(mapping.getFestival()).isEqualTo(festival);
        assertThat(mapping.getTravelCourse()).isEqualTo(course);
        assertThat(mapping.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(mapping.getId().getFestivalId()).isEqualTo(1L);
        assertThat(mapping.getId().getCourseId()).isEqualTo(101L);
    }
}
