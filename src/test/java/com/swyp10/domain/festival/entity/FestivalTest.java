package com.swyp10.domain.festival.entity;

import com.swyp10.domain.region.entity.Region;
import com.swyp10.domain.review.entity.UserReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Festival Entity 테스트")
class FestivalTest {

    @DisplayName("Festival 엔티티 생성")
    @Test
    void createFestival() {
        // given
        Festival festival = Festival.builder()
            .name("서울 벚꽃 축제")
            .startDate(LocalDate.of(2025, 4, 1))
            .endDate(LocalDate.of(2025, 4, 10))
            .theme(FestivalTheme.NATURE)
            .description("서울의 봄을 즐기는 벚꽃 축제")
            .thumbnail("https://cdn.example.com/thumbnail.jpg")
            .build();

        // when
        assertThat(festival.getName()).isEqualTo("서울 벚꽃 축제");
        assertThat(festival.getStartDate()).isEqualTo(LocalDate.of(2025, 4, 1));
        assertThat(festival.getTheme()).isEqualTo(FestivalTheme.NATURE);
    }

    @DisplayName("현재 날짜 기준에 따라 Festival Status 변경")
    @Test
    void getCurrentStatus() {
        // given
        LocalDate today = LocalDate.now();

        Festival upcoming = Festival.builder()
            .startDate(today.plusDays(5))
            .endDate(today.plusDays(10))
            .build();

        Festival active = Festival.builder()
            .startDate(today.minusDays(2))
            .endDate(today.plusDays(2))
            .build();

        Festival ended = Festival.builder()
            .startDate(today.minusDays(10))
            .endDate(today.minusDays(1))
            .build();

        // then
        assertThat(upcoming.getCurrentStatus()).isEqualTo(FestivalStatus.INACTIVE);
        assertThat(active.getCurrentStatus()).isEqualTo(FestivalStatus.ACTIVE);
        assertThat(ended.getCurrentStatus()).isEqualTo(FestivalStatus.ENDED);
    }

    @DisplayName("Festival 엔티티 수정")
    @Test
    void updateFestival() {
        // given
        Festival festival = Festival.builder()
            .name("임시 축제")
            .startDate(LocalDate.of(2025, 5, 1))
            .endDate(LocalDate.of(2025, 5, 10))
            .theme(FestivalTheme.MUSIC)
            .description("임시 설명")
            .thumbnail("img1.jpg")
            .build();

        // when
        festival.updateFestival("정식 축제",
            LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 7),
            FestivalTheme.FOOD, "정식 설명", "img2.jpg");

        // then
        assertThat(festival.getName()).isEqualTo("정식 축제");
        assertThat(festival.getTheme()).isEqualTo(FestivalTheme.FOOD);
    }

    @DisplayName("Festival 엔티티 - TravelCourse, Review 연관 관계 편의 메서드")
    @Test
    void manageAssociations() {
        // given
        Festival festival = Festival.builder()
            .name("부산 불꽃 축제")
            .startDate(LocalDate.of(2025, 10, 20))
            .endDate(LocalDate.of(2025, 10, 21))
            .build();

        FestivalTravelCourse course = FestivalTravelCourse.builder()
            .id(new FestivalTravelCourseId(1L, 2L))
            .build();

        UserReview review = UserReview.builder()
            .content("review content")
            .build();

        Region region = Region.builder()
            .regionCode(26)
            .regionName("부산광역시")
            .build();

        // when
        festival.addTravelCourse(course);
        festival.addReview(review);
        festival.setRegion(region);

        // then
        assertThat(festival.getTravelCourses()).contains(course);
        assertThat(festival.getReviews()).contains(review);
        assertThat(course.getFestival()).isEqualTo(festival);
        assertThat(review.getFestival()).isEqualTo(festival);
        assertThat(region.getFestivals()).contains(festival);
        assertThat(festival.getRegion()).isEqualTo(region);
    }
}