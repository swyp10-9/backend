package com.swyp10.domain.recommendation.service;

import com.swyp10.domain.recommendation.entity.MonthlyRecommendation;
import com.swyp10.domain.recommendation.repository.MonthlyRecommendationRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonthlyRecommendationService 테스트")
class MonthlyRecommendationServiceTest {

    @Mock private MonthlyRecommendationRepository recommendationRepository;
    @InjectMocks private MonthlyRecommendationService recommendationService;

    private MonthlyRecommendation recommendation;

    @BeforeEach
    void setUp() {
        recommendation = MonthlyRecommendation.builder()
            .festivalId(1L)
            .sortSq(10L)
            .festival(null)
            .build();
    }

    @Nested
    @DisplayName("월간 추천 조회")
    class GetTest {

        @Test
        @DisplayName("조회 성공")
        void get_success() {
            given(recommendationRepository.findById(1L)).willReturn(Optional.of(recommendation));

            MonthlyRecommendation result = recommendationService.getMonthlyRecommendation(1L);

            assertThat(result).isEqualTo(recommendation);
            verify(recommendationRepository).findById(1L);
        }

        @Test
        @DisplayName("조회 실패 - 존재하지 않는 ID")
        void get_fail() {
            given(recommendationRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recommendationService.getMonthlyRecommendation(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("월간 추천 생성")
    void create_success() {
        given(recommendationRepository.save(recommendation)).willReturn(recommendation);

        MonthlyRecommendation result = recommendationService.createMonthlyRecommendation(recommendation);

        assertThat(result).isEqualTo(recommendation);
        verify(recommendationRepository).save(recommendation);
    }

    @Test
    @DisplayName("월간 추천 삭제")
    void delete_success() {
        willDoNothing().given(recommendationRepository).deleteById(1L);

        recommendationService.deleteMonthlyRecommendation(1L);

        verify(recommendationRepository).deleteById(1L);
    }
}
