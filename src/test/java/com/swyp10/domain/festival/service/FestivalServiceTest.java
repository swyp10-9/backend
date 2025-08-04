package com.swyp10.domain.festival.service;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

    // ========== 성공 케이스 ==========

    @Test
    @DisplayName("축제 저장 및 단건 조회 - 성공")
    void saveAndFindFestival_success() {
        // given
        SearchFestival2Dto searchDto = SearchFestival2Dto.builder()
            .contentid("TEST-001")
            .title("테스트축제")
            .build();
        DetailCommon2Dto commonDto = DetailCommon2Dto.builder()
            .overview("테스트 개요")
            .build();
        DetailIntro2Dto introDto = DetailIntro2Dto.builder()
            .agelimit("전체관람가")
            .build();
        DetailImage2Dto imageDto = DetailImage2Dto.builder()
            .imgid("IMG-001")
            .originimgurl("http://test.com/img.jpg")
            .smallimageurl("http://test.com/img_s.jpg")
            .serialnum("1")
            .build();

        // when
        Festival saved = festivalService.saveOrUpdateFestival(
            searchDto, commonDto, introDto, List.of(imageDto)
        );

        // then
        assertThat(saved.getContentId()).isEqualTo("TEST-001");
        assertThat(saved.getOverview()).isEqualTo("테스트 개요");
        assertThat(saved.getDetailIntro().getAgelimit()).isEqualTo("전체관람가");
        assertThat(saved.getDetailImages()).hasSize(1);

        Festival find = festivalService.findByContentId("TEST-001");
        assertThat(find).isNotNull();
        assertThat(find.getContentId()).isEqualTo("TEST-001");
    }

    @Test
    @DisplayName("축제 정보 업데이트(UPSERT) - 성공")
    void updateFestival_success() {
        // given
        SearchFestival2Dto searchDto = SearchFestival2Dto.builder()
            .contentid("TEST-002")
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
            .contentid("TEST-003")
            .title("삭제축제")
            .build();
        DetailCommon2Dto commonDto = DetailCommon2Dto.builder().overview("삭제대상").build();
        DetailIntro2Dto introDto = DetailIntro2Dto.builder().agelimit("삭제대상").build();
        Festival saved = festivalService.saveOrUpdateFestival(searchDto, commonDto, introDto, List.of());

        // when
        festivalService.deleteByFestivalId(saved.getFestivalId());

        // then
        boolean exists = festivalService.existsByContentId("TEST-003");
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
}
