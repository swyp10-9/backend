package com.swyp10.domain.travelcourse.service;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import com.swyp10.domain.travelcourse.entity.TravelCourse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("TravelCourseService 테스트")
public class TravelCourseServiceTest {

    @Autowired
    private TravelCourseService travelCourseService;

    @Test
    @DisplayName("여행코스 저장 및 조회 성공")
    void saveAndFindTravelCourse_success() {
        SearchTravelCourseDto searchDto = SearchTravelCourseDto.builder()
            .contentid("TEST-001")
            .title("테스트 여행코스")
            .addr1("테스트 주소")
            .build();

        DetailInfoCourseDto detailDto = DetailInfoCourseDto.builder()
            .serialnum("1")
            .coursename("A 구간")
            .coursedesc("테스트 설명")
            .coursedist("5km")
            .coursestime("2시간")
            .build();

        TravelCourse saved = travelCourseService.saveOrUpdateTravelCourse(searchDto, List.of(detailDto));
        assertThat(saved.getContentId()).isEqualTo("TEST-001");

        TravelCourse found = travelCourseService.findByContentId("TEST-001");
        assertThat(found).isNotNull();
        assertThat(found.getBasicInfo().getTitle()).isEqualTo("테스트 여행코스");
        assertThat(found.getDetailInfos()).hasSize(1);
        assertThat(found.getDetailInfos().get(0).getCoursename()).isEqualTo("A 구간");
    }
}
