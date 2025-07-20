package com.swyp10.domain.festival.entity;

import com.swyp10.domain.region.entity.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FestivalStatistics Entity 테스트")
class FestivalStatisticsTest {

    @Test
    @DisplayName("Festivalstatistics 엔티티 초기 생성 메서드")
    void createEmptyStats() {
        Festival festival = Festival.builder()
            .name("전주 한옥마을 축제")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(3))
            .theme(FestivalTheme.HISTORY)
            .description("전통 축제")
            .thumbnail("img.jpg")
            .build();

        // regionCode 강제 설정 (mock region)
        Region region = Region.builder()
            .regionCode(3)
            .regionName("성수")
            .parentCode("서울")
            .build();
        festival.setRegion(region);

        FestivalStatistics stats = FestivalStatistics.createEmpty(festival);

        assertThat(stats.getFestival()).isEqualTo(festival);
        assertThat(stats.getRegionCode()).isEqualTo(3);
        assertThat(stats.getViewCount()).isZero();
        assertThat(stats.getBookmarkCount()).isZero();
        assertThat(stats.getRatingAvg()).isEqualTo(BigDecimal.ZERO);
        assertThat(stats.getRatingCount()).isZero();
    }

    @Test
    @DisplayName("조회수와 북마크 증가/감소")
    void updateCounts() {
        FestivalStatistics stats = FestivalStatistics.builder()
            .festival(null)
            .regionCode(1)
            .viewCount(0)
            .bookmarkCount(0)
            .ratingAvg(BigDecimal.ZERO)
            .ratingCount(0)
            .build();

        stats.increaseViewCount();
        stats.increaseBookmarkCount();
        stats.increaseBookmarkCount();
        stats.decreaseBookmarkCount();

        assertThat(stats.getViewCount()).isEqualTo(1);
        assertThat(stats.getBookmarkCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("평점을 추가하면 평균 평점과 평점 수 갱신")
    void updateRating() {
        FestivalStatistics stats = FestivalStatistics.builder()
            .festival(null)
            .regionCode(1)
            .viewCount(0)
            .bookmarkCount(0)
            .ratingAvg(BigDecimal.ZERO)
            .ratingCount(0)
            .build();

        stats.updateRating(BigDecimal.valueOf(4.0)); // 첫 평점
        stats.updateRating(BigDecimal.valueOf(5.0)); // 두 번째 평점

        assertThat(stats.getRatingCount()).isEqualTo(2);
        assertThat(stats.getRatingAvg()).isEqualTo(new BigDecimal("4.50")); // scale = 2
    }
}