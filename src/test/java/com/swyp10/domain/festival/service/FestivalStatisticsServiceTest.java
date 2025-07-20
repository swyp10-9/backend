package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.entity.FestivalStatistics;
import com.swyp10.domain.festival.repository.FestivalStatisticsRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FestivalStatisticsService 유닛 테스트")
class FestivalStatisticsServiceTest {

    @Mock
    private FestivalStatisticsRepository statisticsRepository;

    @InjectMocks
    private FestivalStatisticsService statisticsService;

    private FestivalStatistics stats;

    @BeforeEach
    void setUp() {
        stats = FestivalStatistics.builder()
            .festival(null)
            .regionCode(11)
            .viewCount(200)
            .bookmarkCount(100)
            .ratingAvg(BigDecimal.valueOf(4.2))
            .ratingCount(50)
            .build();
    }

    @Nested
    @DisplayName("통계 조회 테스트")
    class GetStatistics {

        @Test
        @DisplayName("축제 ID로 통계 조회 성공")
        void get_statistics_success() {
            given(statisticsRepository.findById(1L)).willReturn(Optional.of(stats));

            FestivalStatistics result = statisticsService.getFestivalStatistics(1L);

            assertThat(result).isEqualTo(stats);
            verify(statisticsRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 통계 ID 조회 시 예외 발생")
        void get_statistics_fail() {
            given(statisticsRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> statisticsService.getFestivalStatistics(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("통계 생성")
    void create_statistics() {
        given(statisticsRepository.save(any())).willReturn(stats);

        FestivalStatistics result = statisticsService.createFestivalStatistics(stats);

        assertThat(result).isEqualTo(stats);
        verify(statisticsRepository).save(stats);
    }

    @Test
    @DisplayName("통계 삭제")
    void delete_statistics() {
        statisticsService.deleteFestivalStatistics(1L);

        verify(statisticsRepository).deleteById(1L);
    }
}
