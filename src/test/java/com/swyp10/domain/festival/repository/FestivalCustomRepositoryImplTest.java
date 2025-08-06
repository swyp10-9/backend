package com.swyp10.domain.festival.repository;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.domain.festival.dto.request.FestivalCalendarRequest;
import com.swyp10.domain.festival.dto.request.FestivalMapRequest;
import com.swyp10.domain.festival.dto.response.FestivalDailyCountResponse;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.enums.FestivalStatus;
import com.swyp10.domain.festival.enums.FestivalTheme;
import com.swyp10.domain.festival.enums.FestivalWithWhom;
import com.swyp10.domain.festival.enums.RegionFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("FestivalCustomRepositoryImpl 테스트")
class FestivalCustomRepositoryImplTest {

    @Autowired
    FestivalRepository festivalRepository;

    @BeforeEach
    void clear(){
        festivalRepository.deleteAll();
    }

    @Nested
    @DisplayName("findFestivalsForMap")
    class FestivalsForMap {

        @Test
        @DisplayName("축제 지도 필터 - 성공")
        void findFestivalsForMap_success() {
            // Given - 테스트 데이터 저장
            FestivalBasicInfo basicInfo = FestivalBasicInfo.builder()
                .title("봄꽃축제")
                .eventstartdate(LocalDate.now().minusDays(2))
                .eventenddate(LocalDate.now().plusDays(2))
                .mapx((double) 127.0)
                .mapy((double) 37.5)
                .build();

            Festival f1 = Festival.builder()
                .contentId("1212")
                .basicInfo(basicInfo)
                .status(FestivalStatus.ONGOING)
                .build();

            festivalRepository.save(f1);

            FestivalMapRequest req = new FestivalMapRequest();
            req.setStatus(FestivalStatus.ONGOING);
            req.setLatTopLeft(38.0);
            req.setLngTopLeft(126.0);
            req.setLatBottomRight(36.0);
            req.setLngBottomRight(128.0);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForMap(req, pageable);

            // Then
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("봄꽃축제");
        }

        @Test
        @DisplayName("축제 지도 필터 - 결과 없음(좌표 범위 외) - 실패")
        void findFestivalsForMap_noResult() {
            // Given
            FestivalMapRequest req = new FestivalMapRequest();
            req.setStatus(FestivalStatus.ONGOING);
            req.setLatTopLeft(50.0);
            req.setLngTopLeft(150.0); // 실제 존재하지 않는 좌표 범위
            req.setLatBottomRight(49.0);
            req.setLngBottomRight(151.0);
            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForMap(req, pageable);

            // Then
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findFestivalsForCalendar")
    class FestivalsForCalendar {
        @Test
        @DisplayName("축제 캘린더 - 조건에 맞는 축제 조회 - 성공")
        void findFestivalsForCalendar_success() {
            // given
            FestivalBasicInfo info = FestivalBasicInfo.builder()
                .title("달력테스트축제")
                .eventstartdate(LocalDate.of(2025, 8, 10))
                .eventenddate(LocalDate.of(2025, 8, 20))
                .build();
            Festival festival = Festival.builder()
                .contentId("11001")
                .basicInfo(info)
                .regionFilter(RegionFilter.SEOUL)
                .withWhom(FestivalWithWhom.FAMILY)
                .theme(FestivalTheme.CULTURE_ART)
                .status(FestivalStatus.ONGOING)
                .build();
            festivalRepository.save(festival);

            FestivalCalendarRequest req = new FestivalCalendarRequest();
            req.setRegion(RegionFilter.SEOUL);
            req.setWithWhom(FestivalWithWhom.FAMILY);
            req.setTheme(FestivalTheme.CULTURE_ART);
            req.setDate(LocalDate.of(2025, 8, 15));

            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForCalendar(req, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("달력테스트축제");
        }

        @Test
        @DisplayName("축제 캘린더 - 조건 불일치 시 결과 없음 - 실패")
        void findFestivalsForCalendar_empty() {
            // given
            FestivalBasicInfo info = FestivalBasicInfo.builder()
                .title("달력테스트축제2")
                .eventstartdate(LocalDate.of(2025, 8, 1))
                .eventenddate(LocalDate.of(2025, 8, 31))
                .build();
            Festival festival = Festival.builder()
                .contentId("1002")
                .basicInfo(info)
                .regionFilter(RegionFilter.CHUNGCHEONG)
                .withWhom(FestivalWithWhom.FRIENDS)
                .theme(FestivalTheme.FOOD)
                .status(FestivalStatus.ONGOING)
                .build();
            festivalRepository.save(festival);

            FestivalCalendarRequest req = new FestivalCalendarRequest();
            req.setRegion(RegionFilter.SEOUL); // 필터 미일치
            req.setDate(LocalDate.of(2025, 8, 15));

            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForCalendar(req, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findDailyFestivalCounts")
    class DailyFestivalCounts {
        @Test
        @DisplayName("날짜별 진행중 축제 개수 집계 - 성공")
        void findDailyFestivalCounts_success() {
            // given
            FestivalBasicInfo info1 = FestivalBasicInfo.builder()
                .title("축제1")
                .eventstartdate(LocalDate.of(2025, 8, 10))
                .eventenddate(LocalDate.of(2025, 8, 15))
                .build();
            Festival f1 = Festival.builder()
                .contentId("1111")
                .basicInfo(info1)
                .build();
            festivalRepository.save(f1);

            FestivalBasicInfo info2 = FestivalBasicInfo.builder()
                .title("축제2")
                .eventstartdate(LocalDate.of(2025, 8, 13))
                .eventenddate(LocalDate.of(2025, 8, 18))
                .build();
            Festival f2 = Festival.builder()
                .contentId("1234")
                .basicInfo(info2)
                .build();
            festivalRepository.save(f2);

            LocalDate start = LocalDate.of(2025, 8, 10);
            LocalDate end = LocalDate.of(2025, 8, 15);

            // when
            List<FestivalDailyCountResponse.DailyCount> counts = festivalRepository.findDailyFestivalCounts(start, end);

            // then
            assertThat(counts).hasSize(6); // 10~15일까지 6개
            // 8/10 ~ 8/12: 1개, 8/13~8/15: 2개
            assertThat(counts.get(0).getCount()).isEqualTo(1); // 8/10
            assertThat(counts.get(3).getCount()).isEqualTo(2); // 8/13
            assertThat(counts.get(5).getCount()).isEqualTo(2); // 8/15
        }

        @Test
        @DisplayName("데이터 결과 없음")
        void findDailyFestivalCounts_empty() {
            // given: DB에 데이터 없음
            LocalDate start = LocalDate.of(2030, 1, 1);
            LocalDate end = LocalDate.of(2030, 1, 5);

            // when
            List<FestivalDailyCountResponse.DailyCount> counts = festivalRepository.findDailyFestivalCounts(start, end);

            // then
            assertThat(counts).hasSize(5);
            assertThat(counts.stream().allMatch(c -> c.getCount() == 0)).isTrue();
        }
    }
}
