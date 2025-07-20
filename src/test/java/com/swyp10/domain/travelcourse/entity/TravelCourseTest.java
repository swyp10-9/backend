package com.swyp10.domain.travelcourse.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TravelCourse Entity 테스트")
class TravelCourseTest {

    @Test
    @DisplayName("TravelCourse 엔티티 생성")
    void createTravelCourse() {
        TravelCourse course = TravelCourse.builder()
            .title("강릉 해변 코스")
            .durationHours(3)
            .difficultyLevel(TravelDifficulty.EASY)
            .build();

        assertThat(course.getTitle()).isEqualTo("강릉 해변 코스");
        assertThat(course.getDurationHours()).isEqualTo(3);
        assertThat(course.getDifficultyLevel()).isEqualTo(TravelDifficulty.EASY);
    }

    @Test
    @DisplayName("TravelCourse 엔티티 수정")
    void updateTravelCourse() {
        TravelCourse course = TravelCourse.builder()
            .title("임시 제목")
            .durationHours(2)
            .difficultyLevel(TravelDifficulty.NORMAL)
            .build();

        course.updateCourse("제주 올레길 코스", 5, TravelDifficulty.HARD);

        assertThat(course.getTitle()).isEqualTo("제주 올레길 코스");
        assertThat(course.getDurationHours()).isEqualTo(5);
        assertThat(course.getDifficultyLevel()).isEqualTo(TravelDifficulty.HARD);
    }
}
