package com.swyp10.domain.festival.repository;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.domain.festival.dto.request.FestivalMapRequest;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.enums.FestivalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("FestivalCustomRepositoryImpl 테스트")
class FestivalCustomRepositoryImplTest {

    @Autowired
    FestivalRepository festivalRepository;

    @Test
    @DisplayName("축제 지도 필터 - 성공")
    void findFestivalsForMap_success() {
        // Given - 테스트 데이터 저장
        FestivalBasicInfo basicInfo = FestivalBasicInfo.builder()
            .title("봄꽃축제")
            .eventstartdate(LocalDate.now().minusDays(2))
            .eventenddate(LocalDate.now().plusDays(2))
            .mapx((long) 127.0)
            .mapy((long) 37.5)
            .build();

        Festival f1 = Festival.builder()
            .contentId("1212")
            .basicInfo(basicInfo)
            .status(FestivalStatus.ONGOING)
            .build();

        festivalRepository.save(f1);

        FestivalMapRequest req = new FestivalMapRequest();
        req.setStatus(FestivalStatus.ONGOING);
        req.setLatTopLeft(38.0); req.setLngTopLeft(126.0);
        req.setLatBottomRight(36.0); req.setLngBottomRight(128.0);

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
        req.setLatTopLeft(50.0); req.setLngTopLeft(150.0); // 실제 존재하지 않는 좌표 범위
        req.setLatBottomRight(49.0); req.setLngBottomRight(151.0);
        PageRequest pageable = PageRequest.of(0, 10);

        // When
        Page<FestivalSummaryResponse> result = festivalRepository.findFestivalsForMap(req, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }
}
