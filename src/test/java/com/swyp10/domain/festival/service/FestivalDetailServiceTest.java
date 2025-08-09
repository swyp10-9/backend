package com.swyp10.domain.festival.service;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.festival.dto.response.FestivalDetailResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.entity.FestivalDetailIntro;
import com.swyp10.domain.festival.entity.FestivalImage;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@EntityScan(basePackages = "com.swyp10.domain")
@Transactional
@DisplayName("FestivalDetailService 테스트")
class FestivalDetailServiceTest {

    @Autowired
    FestivalDetailService festivalDetailService;

    @Autowired
    FestivalRepository festivalRepository;

    @Test
    @DisplayName("축제 상세 조회 - DTO 매핑 성공")
    void getFestivalDetail_success() {
        // given
        Festival saved = festivalRepository.save(buildFestivalAggregate("상세축제"));

        // when
        FestivalDetailResponse res = festivalDetailService.getFestivalDetail(Long.parseLong(saved.getContentId()));

        // then
        assertThat(res.getId()).isEqualTo(Long.parseLong(saved.getContentId()));
        assertThat(res.getTitle()).isEqualTo("상세축제");
        assertThat(res.getAddress()).isEqualTo("서울시 강남구 어딘가");
        assertThat(res.getStartDate()).isEqualTo("2025-09-01");
        assertThat(res.getEndDate()).isEqualTo("2025-09-03");
        assertThat(res.getThumbnail()).isEqualTo("http://thumb/xxx.jpg");
        assertThat(res.getMapx()).isEqualTo("127.0123");
        assertThat(res.getMapy()).isEqualTo("37.1234");

        assertThat(res.getContent()).isNotNull();
        assertThat(res.getContent().getTitle()).isEqualTo("상세축제");
        assertThat(res.getContent().getOverview()).isEqualTo("개요 텍스트");

        assertThat(res.getInfo()).isNotNull();
        assertThat(res.getInfo().getEventplace()).isEqualTo("테스트장소");
        assertThat(res.getImages()).hasSize(2);
        assertThat(res.getImages().get(0).getOriginimgurl()).isEqualTo("http://img/1.jpg");
    }

    @Test
    @DisplayName("축제 상세 조회 - 존재하지 않는 ID면 ApplicationException(NOT_FOUND 계열) 발생")
    void getFestivalDetail_notFound() {
        assertThatThrownBy(() -> festivalDetailService.getFestivalDetail(999999L))
            .isInstanceOf(ApplicationException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FESTIVAL_NOT_FOUND);
    }

    private Festival buildFestivalAggregate(String title) {
        FestivalBasicInfo basic = FestivalBasicInfo.builder()
            .title(title)
            .addr1("서울시 강남구 어딘가")
            .eventstartdate(LocalDate.of(2025, 9, 1))
            .eventenddate(LocalDate.of(2025, 9, 3))
            .firstimage2("http://thumb/xxx.jpg")
            .mapx(127.0123)
            .mapy(37.1234)
            .build();

        FestivalDetailIntro intro = FestivalDetailIntro.builder()
            .sponsor1("스폰서")
            .sponsor1tel("010-0000-0000")
            .eventplace("테스트장소")
            .eventhomepage("<a href='http://example.com'>홈</a>")
            .playtime("09:00~18:00")
            .usetimefestival("무료")
            .build();

        List<FestivalImage> images = List.of(
            FestivalImage.builder()
                .imgid("1111").originimgurl("http://img/1.jpg").smallimageurl("http://img/1_s.jpg").build(),
            FestivalImage.builder()
                .imgid("2222").originimgurl("http://img/2.jpg").smallimageurl("http://img/2_s.jpg").build()
        );

        return Festival.builder()
            .contentId("1234")
            .overview("개요 텍스트")
            .basicInfo(basic)
            .detailIntro(intro)
            .detailImages(images)
            .build();
    }
}
