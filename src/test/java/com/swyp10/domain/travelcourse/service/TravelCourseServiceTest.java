package com.swyp10.domain.travelcourse.service;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.config.TestConfig;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.travelcourse.dto.request.FestivalTravelCoursePageRequest;
import com.swyp10.domain.travelcourse.dto.response.FestivalTravelCourseListResponse;
import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import com.swyp10.domain.travelcourse.entity.TravelCourse;
import com.swyp10.domain.travelcourse.entity.TravelCourseBasicInfo;
import com.swyp10.domain.travelcourse.entity.TravelCourseDetailInfo;
import com.swyp10.domain.travelcourse.repository.TravelCourseRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestConfig.class, QueryDslConfig.class})
@EntityScan("com.swyp10.domain")
@Transactional
@DisplayName("TravelCourseService 테스트")
public class TravelCourseServiceTest {

    @Resource
    private TravelCourseService travelCourseService;

    @Resource
    private FestivalRepository festivalRepository;

    @Resource
    private TravelCourseRepository travelCourseRepository;

    @Test
    @DisplayName("여행코스 저장 및 조회 - 성공")
    void saveAndFindTravelCourse_success() {
        SearchTravelCourseDto searchDto = SearchTravelCourseDto.builder()
            .contentid("11111")
            .title("테스트 여행코스")
            .addr1("테스트 주소")
            .build();

        DetailInfoCourseDto detailDto = DetailInfoCourseDto.builder()
            .subname("A 구간")
            .subdetailimg("테스트 설명")
            .build();

        TravelCourse saved = travelCourseService.saveOrUpdateTravelCourse(searchDto, List.of(detailDto));
        assertThat(saved.getContentId()).isEqualTo("11111");

        TravelCourse found = travelCourseService.findByContentId("11111");
        assertThat(found).isNotNull();
        assertThat(found.getBasicInfo().getTitle()).isEqualTo("테스트 여행코스");
        assertThat(found.getDetailInfos()).hasSize(1);
        assertThat(found.getDetailInfos().get(0).getSubname()).isEqualTo("A 구간");
    }

    @Test
    @DisplayName("festivalId → areacode 매칭 코스 1건과 detailInfos를 근처 볼거리로 매핑 - 성공")
    void getFestivalTravelCourses_success() {
        // given: festival 저장 (areacode=11)
        Festival festival = Festival.builder()
            .contentId("12345")
            .basicInfo(FestivalBasicInfo.builder()
                .title("축제")
                .areacode("11")
                .addr1("서울")
                .mapx(127.00)
                .mapy(37.50)
                .build())
            .build();
        festivalRepository.save(festival);

        // 해당 지역 TravelCourse 저장
        TravelCourse course = TravelCourse.builder()
            .contentId("111111")
            .basicInfo(TravelCourseBasicInfo.builder()
                .areacode("11")
                .title("서울 도심 코스")
                .mapx("127.10")
                .mapy("37.55")
                .build())
            .build();
        course.addDetailInfo(TravelCourseDetailInfo.builder()
            .subnum("1").subcontentid("S-1").subname("청계천 산책").subdetailoverview("좋음").subdetailimg("https://a.jpg").build());
        course.addDetailInfo(TravelCourseDetailInfo.builder()
            .subnum("2").subcontentid("S-2").subname("북촌 한옥마을").subdetailoverview("아름다움").subdetailimg("https://b.jpg").build());
        travelCourseRepository.save(course);

        FestivalTravelCoursePageRequest req = FestivalTravelCoursePageRequest.builder()
            .festivalId(festival.getFestivalId())
            .build();

        // when
        FestivalTravelCourseListResponse res = travelCourseService.getFestivalTravelCourses(req);

        // then
        assertThat(res.getCourses()).hasSize(1);
        assertThat(res.getCourses().get(0).getTitle()).isEqualTo("서울 도심 코스");
        assertThat(res.getNearbyAttractions()).hasSize(2);
        assertThat(res.getNearbyAttractions().get(0).getName()).isEqualTo("청계천 산책");
        assertThat(res.getNearbyAttractions().get(0).getMapx()).isEqualTo("127.10");
        assertThat(res.getNearbyAttractions().get(0).getMapy()).isEqualTo("37.55");
    }

    @Test
    @DisplayName("festivalId가 존재하지 않으면 ApplicationException(FESTIVAL_NOT_FOUND) - 실패")
    void getFestivalTravelCourses_festivalNotFound() {
        // given
        FestivalTravelCoursePageRequest req = FestivalTravelCoursePageRequest.builder()
            .festivalId(999999L)
            .build();

        // expect
        assertThatThrownBy(() -> travelCourseService.getFestivalTravelCourses(req))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining("Festival not found")
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FESTIVAL_NOT_FOUND);
    }

    @Test
    @DisplayName("festival에 areacode가 없으면 ApplicationException(TRAVEL_COURSE_NOT_FOUND) - 실패")
    void getFestivalTravelCourses_noAreaCode() {
        // given: areacode null 인 festival
        Festival festival = Festival.builder()
            .contentId("111111")
            .basicInfo(FestivalBasicInfo.builder()
                .title("지역코드 없음 축제")
                .areacode(null)
                .build())
            .build();
        festivalRepository.save(festival);

        FestivalTravelCoursePageRequest req = FestivalTravelCoursePageRequest.builder()
            .festivalId(festival.getFestivalId())
            .build();

        // expect
        assertThatThrownBy(() -> travelCourseService.getFestivalTravelCourses(req))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining("Festival has no areaCode")
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRAVEL_COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("지역에 코스가 없으면 빈 리스트 반환")
    void getFestivalTravelCourses_noCourseInArea_returnsEmpty() {
        // given: festival with areacode=33, 하지만 해당 지역 코스는 저장하지 않음
        Festival festival = Festival.builder()
            .contentId("111111")
            .basicInfo(FestivalBasicInfo.builder()
                .title("축제")
                .areacode("33")
                .build())
            .build();
        festivalRepository.save(festival);

        FestivalTravelCoursePageRequest req = FestivalTravelCoursePageRequest.builder()
            .festivalId(festival.getFestivalId())
            .build();

        // when
        FestivalTravelCourseListResponse res = travelCourseService.getFestivalTravelCourses(req);

        // then
        assertThat(res.getCourses()).isEmpty();
        assertThat(res.getNearbyAttractions()).isEmpty();
    }

    @Test
    @DisplayName("코스는 있으나 detailInfos가 없으면 nearbyAttractions는 빈 리스트")
    void getFestivalTravelCourses_courseWithoutDetails() {
        // given
        Festival festival = Festival.builder()
            .contentId("111111")
            .basicInfo(FestivalBasicInfo.builder()
                .title("축제")
                .areacode("44")
                .build())
            .build();
        festivalRepository.save(festival);

        TravelCourse course = TravelCourse.builder()
            .contentId("222222")
            .basicInfo(TravelCourseBasicInfo.builder()
                .areacode("44")
                .title("코스 - 디테일 없음")
                .mapx("127.30")
                .mapy("37.70")
                .build())
            .build();
        travelCourseRepository.save(course);

        FestivalTravelCoursePageRequest req = FestivalTravelCoursePageRequest.builder()
            .festivalId(festival.getFestivalId())
            .build();

        // when
        FestivalTravelCourseListResponse res = travelCourseService.getFestivalTravelCourses(req);

        // then
        assertThat(res.getCourses()).hasSize(1);
        assertThat(res.getNearbyAttractions()).isEmpty();
    }
}
