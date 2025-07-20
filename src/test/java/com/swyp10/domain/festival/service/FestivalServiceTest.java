package com.swyp10.domain.festival.service;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.repository.FestivalRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FestivalService 유닛 테스트")
class FestivalServiceTest {

    @Mock
    private FestivalRepository festivalRepository;

    @InjectMocks
    private FestivalService festivalService;

    private Festival testFestival;

    @BeforeEach
    void setUp() {
        testFestival = Festival.builder()
            .id(1L)
            .name("서울 불꽃축제")
            .startDate(LocalDate.of(2025, 10, 1))
            .endDate(LocalDate.of(2025, 10, 2))
            .build();
    }

    @Nested
    @DisplayName("축제 조회 테스트")
    class GetFestivalTest {

        @Test
        @DisplayName("ID로 축제 조회 성공")
        void get_festival_success() {
            // given
            given(festivalRepository.findById(1L)).willReturn(Optional.of(testFestival));

            // when
            Festival result = festivalService.getFestival(1L);

            // then
            assertThat(result).isEqualTo(testFestival);
            verify(festivalRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
        void get_festival_fail() {
            // given
            given(festivalRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> festivalService.getFestival(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("축제 생성")
    void create_festival() {
        // given
        given(festivalRepository.save(any(Festival.class))).willReturn(testFestival);

        // when
        Festival result = festivalService.createFestival(testFestival);

        // then
        assertThat(result).isEqualTo(testFestival);
        verify(festivalRepository).save(testFestival);
    }

    @Test
    @DisplayName("축제 삭제")
    void delete_festival() {
        // when
        festivalService.deleteFestival(1L);

        // then
        verify(festivalRepository).deleteById(1L);
    }
}
