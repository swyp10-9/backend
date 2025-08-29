package com.swyp10.domain.festival.service;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.config.TestConfig;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.bookmark.entity.UserBookmark;
import com.swyp10.domain.bookmark.repository.UserBookmarkRepository;
import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalDailyCountResponse;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.dto.response.FestivalMonthlyTopListResponse;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
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
import com.swyp10.global.page.PageRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestConfig.class, QueryDslConfig.class})
@Transactional
@DisplayName("FestivalService 테스트")
class FestivalServiceTest {

    @Autowired
    FestivalService festivalService;

    @Autowired
    FestivalRepository festivalRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserBookmarkRepository userBookmarkRepository;

    @Autowired
    EntityManager em;

    @BeforeEach
    void cleanUp() {
        festivalRepository.deleteAll();
    }

    private User saveUser(String email, String nickname) {
        User u = User.builder()
            .email(email)
            .password("pw")
            .nickname(nickname)
            .signupCompleted(true)
            .build();
        return userRepository.save(u);
    }

    private Festival saveFestival(String contentId, String title, double mapx, double mapy) {
        FestivalBasicInfo basic = FestivalBasicInfo.builder()
            .title(title)
            .eventstartdate(LocalDate.now())
            .eventenddate(LocalDate.now().plusDays(2))
            .firstimage2("https://thumb.test/" + contentId + ".jpg")
            .addr1("서울시 어딘가")
            .mapx(mapx)
            .mapy(mapy)
            .build();

        Festival f = Festival.builder()
            .contentId(contentId)
            .basicInfo(basic)
            .build();

        return festivalRepository.save(f);
    }

    private UserBookmark saveBookmark(User user, Festival festival, LocalDateTime createdAt, boolean softDeleted) {
        UserBookmark ub = UserBookmark.builder()
            .user(user)
            .festival(festival)
            .build();
        // 생성 시각/삭제 시각 제어
        // createdAt은 @PrePersist로 now가 들어가지만 통합 테스트에서 순서 구분을 위해 직접 세팅
        userBookmarkRepository.saveAndFlush(ub);
        em.flush();
        em.clear();

        // createdAt & deletedAt 직접 세팅
        UserBookmark managed = userBookmarkRepository.findByUser_UserIdAndFestival_FestivalId(
            user.getUserId(), festival.getFestivalId()
        ).orElseThrow();

        // createdAt 강제 세팅
        try {
            var createdField = UserBookmark.class.getDeclaredField("createdAt");
            createdField.setAccessible(true);
            createdField.set(managed, createdAt);
        } catch (Exception ignore) {}

        if (softDeleted) {
            managed.markDeleted();
        }
        em.flush();
        em.clear();

        return managed;
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
            FestivalListResponse res = festivalService.getFestivalsForMap(null, req);

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
            FestivalListResponse res = festivalService.getFestivalsForMap(null, req);

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
            FestivalListResponse result = festivalService.getFestivalsForCalendar(null, request);

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
            FestivalListResponse result = festivalService.getFestivalsForCalendar(null, request);

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
            FestivalListResponse result = festivalService.getFestivalsForPersonalTest(null, request);

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
            FestivalListResponse result = festivalService.getFestivalsForPersonalTest(null, request);

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
            FestivalListResponse response = festivalService.searchFestivals(null, req);

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
            FestivalListResponse response = festivalService.searchFestivals(null, req);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
        }

    }

    @Nested
    @DisplayName("getMyBookmarkedFestivals 통합 테스트")
    class BookmarkedFestivals {

        @Test
        @DisplayName("북마크 2건 존재 → 최신순(createdAt DESC)으로 반환, bookmarked=true 세팅")
        void list_success_sorted_desc() {
            // given
            User user = saveUser("user1@test.com", "유저1");
            Festival f1 = saveFestival("111", "축제-오래된", 127.01, 37.51);
            Festival f2 = saveFestival("222", "축제-최신", 127.02, 37.52);

            // 오래된 → 최신 순서로 createdAt 세팅
            saveBookmark(user, f1, LocalDateTime.now().minusDays(1), false);
            saveBookmark(user, f2, LocalDateTime.now(), false);

            FestivalMyPageRequest req = FestivalMyPageRequest.builder()
                .page(0).size(10)
                .build();

            // when
            FestivalListResponse res = festivalService.getMyBookmarkedFestivals(user.getUserId(), req);

            // then
            assertThat(res.getContent()).hasSize(2);
            // 최신이 먼저
            assertThat(res.getContent().get(0).getTitle()).isEqualTo("축제-최신");
            assertThat(res.getContent().get(1).getTitle()).isEqualTo("축제-오래된");
            // 서비스에서 bookmarked = true 로 마킹
            assertThat(res.getContent().get(0).getBookmarked()).isTrue();
            assertThat(res.getContent().get(1).getBookmarked()).isTrue();
            assertThat(res.getTotalElements()).isEqualTo(2);
            assertThat(res.getPage()).isEqualTo(0);
            assertThat(res.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("soft-deleted 북마크는 제외된다")
        void list_excludes_soft_deleted() {
            // given
            User user = saveUser("user2@test.com", "유저2");
            Festival active = saveFestival("111", "보여야함", 126.9, 37.6);
            Festival deleted = saveFestival("222", "보이면안됨", 126.8, 37.5);

            saveBookmark(user, active, LocalDateTime.now(), false);      // 정상
            saveBookmark(user, deleted, LocalDateTime.now(), true);      // soft-deleted

            FestivalMyPageRequest req = FestivalMyPageRequest.builder()
                .page(0).size(10)
                .build();

            // when
            FestivalListResponse res = festivalService.getMyBookmarkedFestivals(user.getUserId(), req);

            // then
            assertThat(res.getContent()).hasSize(1);
            assertThat(res.getContent().get(0).getTitle()).isEqualTo("보여야함");
            assertThat(res.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("북마크가 하나도 없으면 빈 페이지")
        void list_empty() {
            // given
            User user = saveUser("user3@test.com", "유저3");
            FestivalMyPageRequest req = FestivalMyPageRequest.builder()
                .page(0).size(10)
                .build();

            // when
            FestivalListResponse res = festivalService.getMyBookmarkedFestivals(user.getUserId(), req);

            // then
            assertThat(res.getContent()).isEmpty();
            assertThat(res.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("이달의 축제 Service 테스트")
    class MonthlyTopFestivals {

        private Festival saveFestivalWithStatistics(String contentId, String title, int viewCount) {
            FestivalBasicInfo basicInfo = FestivalBasicInfo.builder()
                .title(title)
                .eventstartdate(LocalDate.now().withDayOfMonth(1)) // 이번 달 시작일
                .eventenddate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())) // 이번 달 마지막일
                .firstimage2("https://thumb.test/" + contentId + ".jpg")
                .addr1("서울시 어딘가")
                .mapx(127.0)
                .mapy(37.5)
                .build();

            Festival festival = Festival.builder()
                .contentId(contentId)
                .basicInfo(basicInfo)
                .status(FestivalStatus.ONGOING)
                .build();

            Festival savedFestival = festivalRepository.save(festival);

            // Statistics 초기화 및 viewCount 설정
            savedFestival.initializeStatistics();
            for (int i = 0; i < viewCount; i++) {
                savedFestival.getStatistics().incrementViewCount();
            }

            festivalRepository.save(savedFestival);
            em.flush();
            em.clear();

            return savedFestival;
        }

        @Test
        @DisplayName("이달의 축제 조회 - viewCount 순으로 상위 5개 성공")
        void getMonthlyTopFestivals_success() {
            // given
            saveFestivalWithStatistics("1111", "가장 인기있는 축제", 100);
            saveFestivalWithStatistics("2222", "두번째 인기 축제", 80);
            saveFestivalWithStatistics("3333", "세번째 인기 축제", 60);
            saveFestivalWithStatistics("4444", "네번째 인기 축제", 40);
            saveFestivalWithStatistics("5555", "다섯번째 인기 축제", 20);
            saveFestivalWithStatistics("6666", "여섯번째 인기 축제", 10); // 6개 생성하여 5개만 반환되는지 확인

            // when
            FestivalMonthlyTopListResponse result = festivalService.getMonthlyTopFestivals(null);

            // then
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("가장 인기있는 축제");
            assertThat(result.getContent().get(1).getTitle()).isEqualTo("두번째 인기 축제");
            assertThat(result.getContent().get(2).getTitle()).isEqualTo("세번째 인기 축제");
            assertThat(result.getContent().get(3).getTitle()).isEqualTo("네번째 인기 축제");
            assertThat(result.getContent().get(4).getTitle()).isEqualTo("다섯번째 인기 축제");

            // 페이징 정보 확인
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getFirst()).isTrue();
            assertThat(result.getLast()).isTrue();

            // 로그인하지 않은 사용자이므로 모든 북마크가 false
            result.getContent().forEach(festival ->
                assertThat(festival.getBookmarked()).isFalse());
        }

        @Test
        @DisplayName("이달의 축제 조회 - 로그인한 사용자의 북마크 상태 확인")
        void getMonthlyTopFestivals_with_bookmark_status() {
            // given
            User user = saveUser("user@test.com", "테스트유저");

            Festival festival1 = saveFestivalWithStatistics("1111", "북마크한 축제", 100);
            Festival festival2 = saveFestivalWithStatistics("2222", "북마크하지 않은 축제", 80);

            // 첫 번째 축제만 북마크
            saveBookmark(user, festival1, LocalDateTime.now(), false);

            // when
            FestivalMonthlyTopListResponse result = festivalService.getMonthlyTopFestivals(user.getUserId());

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getBookmarked()).isTrue(); // 북마크한 축제
            assertThat(result.getContent().get(1).getBookmarked()).isFalse(); // 북마크하지 않은 축제
        }

        @Test
        @DisplayName("이달의 축제 조회 - 이번 달에 진행되지 않는 축제는 제외")
        void getMonthlyTopFestivals_excludes_festivals_not_in_current_month() {
            // given
            LocalDate nextMonth = LocalDate.now().plusMonths(1);

            // 이번 달 축제
            saveFestivalWithStatistics("1111", "이번 달 축제", 100);

            // 다음 달 축제 (제외되어야 함)
            FestivalBasicInfo nextMonthBasicInfo = FestivalBasicInfo.builder()
                .title("다음 달 축제")
                .eventstartdate(nextMonth.withDayOfMonth(1))
                .eventenddate(nextMonth.withDayOfMonth(nextMonth.lengthOfMonth()))
                .firstimage2("https://thumb.test/next.jpg")
                .addr1("서울시 어딘가")
                .mapx(127.0)
                .mapy(37.5)
                .build();

            Festival nextMonthFestival = Festival.builder()
                .contentId("2222")
                .basicInfo(nextMonthBasicInfo)
                .status(FestivalStatus.ONGOING)
                .build();

            festivalRepository.save(nextMonthFestival);

            // when
            FestivalMonthlyTopListResponse result = festivalService.getMonthlyTopFestivals(null);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("이번 달 축제");
        }

        @Test
        @DisplayName("이달의 축제 조회 - 통계가 없는 축제도 포함 (viewCount 0으로 처리)")
        void getMonthlyTopFestivals_includes_festivals_without_statistics() {
            // given
            // 통계가 있는 축제
            saveFestivalWithStatistics("1111", "통계 있는 축제", 50);

            // 통계가 없는 축제
            FestivalBasicInfo basicInfo = FestivalBasicInfo.builder()
                .title("통계 없는 축제")
                .eventstartdate(LocalDate.now().withDayOfMonth(1))
                .eventenddate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()))
                .firstimage2("https://thumb.test/no-stats.jpg")
                .addr1("서울시 어딘가")
                .mapx(127.0)
                .mapy(37.5)
                .build();

            Festival festivalWithoutStats = Festival.builder()
                .contentId("2222")
                .basicInfo(basicInfo)
                .status(FestivalStatus.ONGOING)
                .build();

            festivalRepository.save(festivalWithoutStats);

            // when
            FestivalMonthlyTopListResponse result = festivalService.getMonthlyTopFestivals(null);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("통계 있는 축제"); // viewCount가 높으므로 먼저
            assertThat(result.getContent().get(1).getTitle()).isEqualTo("통계 없는 축제"); // viewCount 0으로 나중
        }

        @Test
        @DisplayName("이달의 축제 조회 - 결과 없음")
        void getMonthlyTopFestivals_empty() {
            // given
            // 데이터 없이 테스트

            // when
            FestivalMonthlyTopListResponse result = festivalService.getMonthlyTopFestivals(null);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getEmpty()).isTrue();
            assertThat(result.getFirst()).isTrue();
            assertThat(result.getLast()).isTrue();
        }

        @Test
        @DisplayName("이달의 축제 조회 - 5개 미만일 때 정상 처리")
        void getMonthlyTopFestivals_less_than_five() {
            // given
            saveFestivalWithStatistics("1111", "첫 번째 축제", 30);
            saveFestivalWithStatistics("2222", "두 번째 축제", 20);
            saveFestivalWithStatistics("3333", "세 번째 축제", 10);

            // when
            FestivalMonthlyTopListResponse result = festivalService.getMonthlyTopFestivals(null);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("첫 번째 축제");
            assertThat(result.getContent().get(1).getTitle()).isEqualTo("두 번째 축제");
            assertThat(result.getContent().get(2).getTitle()).isEqualTo("세 번째 축제");
            assertThat(result.getTotalElements()).isEqualTo(3);
        }
    }
}
