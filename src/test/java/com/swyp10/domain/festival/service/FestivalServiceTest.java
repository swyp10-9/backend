package com.swyp10.domain.festival.service;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.festival.dto.request.FestivalCalendarRequest;
import com.swyp10.domain.festival.dto.request.FestivalMapRequest;
import com.swyp10.domain.festival.dto.request.FestivalPersonalTestRequest;
import com.swyp10.domain.festival.dto.request.FestivalSearchRequest;
import com.swyp10.domain.festival.dto.response.FestivalDailyCountResponse;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.enums.FestivalPersonalityType;
import com.swyp10.domain.festival.enums.FestivalStatus;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("FestivalService 테스트")
class FestivalServiceTest {

    @Autowired
    FestivalService festivalService;

    @Autowired
    FestivalRepository festivalRepository;

    @BeforeEach
    void cleanUp() {
        festivalRepository.deleteAll();
    }

    // ========== 성공 케이스 ==========

    @Test
    @DisplayName("축제 저장 및 단건 조회 - 성공")
    void saveAndFindFestival_success() {
        // given
        SearchFestival2Dto searchDto = SearchFestival2Dto.builder()
            .contentid("1111")
            .title("테스트축제")
            .build();
        DetailCommon2Dto commonDto = DetailCommon2Dto.builder()
            .overview("테스트 개요")
            .build();
        DetailIntro2Dto introDto = DetailIntro2Dto.builder()
            .agelimit("전체관람가")
            .build();
        DetailImage2Dto imageDto = DetailImage2Dto.builder()
            .imgid("1234")
            .originimgurl("http://test.com/img.jpg")
            .smallimageurl("http://test.com/img_s.jpg")
            .serialnum("1")
            .build();

        // when
        Festival saved = festivalService.saveOrUpdateFestival(
            searchDto, commonDto, introDto, List.of(imageDto)
        );

        // then
        assertThat(saved.getContentId()).isEqualTo("1111");
        assertThat(saved.getOverview()).isEqualTo("테스트 개요");
        assertThat(saved.getDetailIntro().getAgelimit()).isEqualTo("전체관람가");
        assertThat(saved.getDetailImages()).hasSize(1);

        Festival find = festivalService.findByContentId("1111");
        assertThat(find).isNotNull();
        assertThat(find.getContentId()).isEqualTo("1111");
    }

    @Test
    @DisplayName("축제 정보 업데이트(UPSERT) - 성공")
    void updateFestival_success() {
        // given
        SearchFestival2Dto searchDto = SearchFestival2Dto.builder()
            .contentid("1111")
            .title("업데이트전")
            .eventstartdate("20240101")
            .eventenddate("20240103")
            .build();
        DetailCommon2Dto commonDto = DetailCommon2Dto.builder()
            .overview("OLD")
            .build();
        DetailIntro2Dto introDto = DetailIntro2Dto.builder()
            .agelimit("OLD")
            .build();

        // save
        festivalService.saveOrUpdateFestival(searchDto, commonDto, introDto, List.of());

        // when - update
        DetailCommon2Dto commonDto2 = DetailCommon2Dto.builder()
            .overview("NEW")
            .build();
        DetailIntro2Dto introDto2 = DetailIntro2Dto.builder()
            .agelimit("NEW")
            .build();
        Festival updated = festivalService.saveOrUpdateFestival(searchDto, commonDto2, introDto2, List.of());

        // then
        assertThat(updated.getOverview()).isEqualTo("NEW");
        assertThat(updated.getDetailIntro().getAgelimit()).isEqualTo("NEW");
    }

    @Test
    @DisplayName("축제 삭제 - 성공")
    void deleteFestival_success() {
        // given
        SearchFestival2Dto searchDto = SearchFestival2Dto.builder()
            .contentid("1111")
            .title("삭제축제")
            .build();
        DetailCommon2Dto commonDto = DetailCommon2Dto.builder().overview("삭제대상").build();
        DetailIntro2Dto introDto = DetailIntro2Dto.builder().agelimit("삭제대상").build();
        Festival saved = festivalService.saveOrUpdateFestival(searchDto, commonDto, introDto, List.of());

        // when
        festivalService.deleteByFestivalId(saved.getFestivalId());

        // then
        boolean exists = festivalService.existsByContentId("1111");
        assertThat(exists).isFalse();
    }

    // ========== 실패/예외 케이스 ==========

    @Test
    @DisplayName("없는 contentId로 축제 조회시 ApplicationException 발생 - 실패")
    void findByContentId_notFound_fail() {
        // expect
        assertThatThrownBy(() -> festivalService.findByContentId("NOT_EXIST_ID"))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining("축제를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("없는 festivalId로 축제 삭제해도 예외 없음")
    void deleteById_notFound_noException() {
        // when/then
        festivalService.deleteByFestivalId(999999L);
    }

    @Nested
    @DisplayName("축제 지도 Service 테스트")
    class FestivalMap{
        @Test
        @DisplayName("축제 리스트 조회(지도 페이지) - 성공")
        void getFestivalsForMap_success() {
            // given
            FestivalBasicInfo basicInfo = FestivalBasicInfo.builder()
                .title("통합테스트축제")
                .eventstartdate(LocalDate.now())
                .eventenddate(LocalDate.now().plusDays(1))
                .mapx((double) 127.1)
                .mapy((double) 37.6)
                .build();

            Festival festival = Festival.builder()
                .contentId("1111")
                .basicInfo(basicInfo)
                .status(FestivalStatus.ONGOING)
                .build();

            festivalRepository.save(festival);

            FestivalMapRequest req = new FestivalMapRequest();
            req.setLatTopLeft(38.0);
            req.setLngTopLeft(126.0);
            req.setLatBottomRight(36.0);
            req.setLngBottomRight(128.0);

            // when
            FestivalListResponse res = festivalService.getFestivalsForMap(req);

            // then
            assertThat(res.getContent()).isNotEmpty();
            assertThat(res.getContent().get(0).getTitle()).isEqualTo("통합테스트축제");
        }

        @Test
        @DisplayName("축제 리스트 조회(지도 페이지) - 결과 없음")
        void getFestivalsForMap_empty() {
            // given
            // 데이터 없이 테스트
            FestivalMapRequest req = new FestivalMapRequest();
            req.setLatTopLeft(38.0);
            req.setLngTopLeft(126.0);
            req.setLatBottomRight(36.0);
            req.setLngBottomRight(128.0);

            // when
            FestivalListResponse res = festivalService.getFestivalsForMap(req);

            // then
            assertThat(res.getContent()).isEmpty();
            assertThat(res.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("축제 캘린더 Service 테스트")
    class FestivalCalendar{
        @Test
        @DisplayName("축제 리스트 조회(달력 페이지) - 성공")
        void getFestivalsForCalendar_success() {
            // given
            LocalDate baseDate = LocalDate.of(2025, 8, 15);
            FestivalBasicInfo basicInfo = FestivalBasicInfo.builder()
                .title("8월의 축제")
                .eventstartdate(baseDate.minusDays(2))
                .eventenddate(baseDate.plusDays(2))
                .build();
            Festival festival = Festival.builder()
                .contentId("1111")
                .basicInfo(basicInfo)
                .status(FestivalStatus.ONGOING)
                .build();
            festivalRepository.save(festival);

            FestivalCalendarRequest request = new FestivalCalendarRequest();
            request.setDate(baseDate);

            // when
            FestivalListResponse result = festivalService.getFestivalsForCalendar(request);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("8월의 축제");
        }

        @Test
        @DisplayName("축제 리스트 조회(달력 페이지) - 결과 없음")
        void getFestivalsForCalendar_empty() {
            // given
            FestivalCalendarRequest request = new FestivalCalendarRequest();
            request.setDate(LocalDate.of(2099, 1, 1)); // 먼 미래에 데이터 없음

            // when
            FestivalListResponse result = festivalService.getFestivalsForCalendar(request);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("일별 축제 카운트 Service 테스트")
    class DailyFestivalCount {
        @Test
        @DisplayName("일별 축제 개수 조회 - 성공")
        void getDailyFestivalCount_success() {
            // given
            LocalDate start = LocalDate.of(2025, 8, 1);
            LocalDate end = LocalDate.of(2025, 8, 3);

            Festival festival = Festival.builder()
                .contentId("1111")
                .basicInfo(FestivalBasicInfo.builder()
                    .title("3일간의 여름축제")
                    .eventstartdate(start)
                    .eventenddate(end)
                    .build())
                .status(FestivalStatus.ONGOING)
                .build();
            festivalRepository.save(festival);

            // when
            FestivalDailyCountResponse result = festivalService.getDailyFestivalCount(start, end);

            // then
            assertThat(result.getDailyCounts()).hasSize(3);
            for (FestivalDailyCountResponse.DailyCount dc : result.getDailyCounts()) {
                assertThat(dc.getCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("일별 축제 개수 조회 - 결과 없음")
        void getDailyFestivalCount_empty() {
            // given
            LocalDate start = LocalDate.of(2099, 8, 1);
            LocalDate end = LocalDate.of(2099, 8, 5);

            // when
            FestivalDailyCountResponse result = festivalService.getDailyFestivalCount(start, end);

            // then
            assertThat(result.getDailyCounts()).hasSize(5);
            for (FestivalDailyCountResponse.DailyCount dc : result.getDailyCounts()) {
                assertThat(dc.getCount()).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("맞춤 성향 테스트 축제 Service 테스트")
    class FestivalPersonalTest {
        @Test
        @DisplayName("성향별 축제 리스트 조회 - 성공")
        void getFestivalsForPersonalTest_success() {
            // given
            FestivalBasicInfo basicInfo = FestivalBasicInfo.builder()
                .title("열정가의 여름축제")
                .eventstartdate(LocalDate.of(2025, 8, 1))
                .eventenddate(LocalDate.of(2025, 8, 5))
                .build();

            Festival festival = Festival.builder()
                .contentId("1111")
                .basicInfo(basicInfo)
                .personalityType(FestivalPersonalityType.ENERGIZER)
                .status(FestivalStatus.ONGOING)
                .build();

            festivalRepository.save(festival);

            FestivalPersonalTestRequest request = new FestivalPersonalTestRequest();
            request.setPersonalityType(FestivalPersonalityType.ENERGIZER);

            // when
            FestivalListResponse result = festivalService.getFestivalsForPersonalTest(request);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("열정가의 여름축제");
        }

        @Test
        @DisplayName("성향별 축제 리스트 조회 - 해당 성향 없음 (빈 결과)")
        void getFestivalsForPersonalTest_empty() {
            // given
            FestivalPersonalTestRequest request = new FestivalPersonalTestRequest();
            request.setPersonalityType(FestivalPersonalityType.ENERGIZER);

            // when
            FestivalListResponse result = festivalService.getFestivalsForPersonalTest(request);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("축제 검색 Service 테스트")
    class FestivalSearchTest {
        @Test
        @DisplayName("축제 리스트 조회(검색 페이지) - 성공")
        void searchFestivals_success() {
            // given
            FestivalBasicInfo basicInfo = FestivalBasicInfo.builder()
                .title("여름 페스티벌")
                .addr1("부산광역시")
                .build();
            Festival festival = Festival.builder()
                .contentId("9999")
                .overview("신나는 여름 축제")
                .basicInfo(basicInfo)
                .build();
            festivalRepository.save(festival);

            FestivalSearchRequest req = new FestivalSearchRequest();
            req.setSearchParam("여름");

            // when
            FestivalListResponse response = festivalService.searchFestivals(req);

            // then
            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getContent().get(0).getTitle()).contains("여름");
        }

        @Test
        @DisplayName("축제 조회 리스트(검색 페이지) - 결과 없음")
        void searchFestivals_empty() {
            // given
            FestivalSearchRequest req = new FestivalSearchRequest();
            req.setSearchParam("이상한검색어");

            // when
            FestivalListResponse response = festivalService.searchFestivals(req);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }
}
