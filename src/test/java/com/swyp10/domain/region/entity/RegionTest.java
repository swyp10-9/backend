package com.swyp10.domain.region.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Region Entity 테스트")
class RegionTest {

    @Test
    @DisplayName("Region 엔티티 생성")
    void createRegion() {
        Region region = Region.builder()
            .regionCode(11)
            .regionName("서울특별시")
            .parentCode("00")
            .build();

        assertThat(region.getRegionCode()).isEqualTo(11);
        assertThat(region.getRegionName()).isEqualTo("서울특별시");
        assertThat(region.getFestivals()).isEmpty();
    }
}
