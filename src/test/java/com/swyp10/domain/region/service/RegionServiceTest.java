package com.swyp10.domain.region.service;

import com.swyp10.domain.region.entity.Region;
import com.swyp10.domain.region.repository.RegionRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionService 테스트")
class RegionServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionService regionService;

    private Region region;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .regionCode(1)
            .regionName("서울특별시")
            .parentCode("00")
            .build();
    }

    @Nested
    @DisplayName("Region 조회")
    class GetRegion {

        @Test
        @DisplayName("정상적으로 Region 조회")
        void getRegionSuccess() {
            // given
            given(regionRepository.findById(1)).willReturn(Optional.of(region));

            // when
            Region result = regionService.getRegion(1);

            // then
            assertThat(result).isEqualTo(region);
            verify(regionRepository).findById(1);
        }

        @Test
        @DisplayName("존재하지 않는 Region 조회 시 예외 발생")
        void getRegionFail() {
            // given
            given(regionRepository.findById(999)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> regionService.getRegion(999))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Region 생성")
    class CreateRegion {

        @Test
        @DisplayName("정상적으로 Region 생성")
        void createRegionSuccess() {
            // given
            given(regionRepository.save(region)).willReturn(region);

            // when
            Region saved = regionService.createRegion(region);

            // then
            assertThat(saved).isEqualTo(region);
            verify(regionRepository).save(region);
        }
    }

    @Nested
    @DisplayName("Region 삭제")
    class DeleteRegion {

        @Test
        @DisplayName("정상적으로 Region 삭제")
        void deleteRegionSuccess() {
            // when
            regionService.deleteRegion(1);

            // then
            verify(regionRepository).deleteById(1);
        }
    }
}
