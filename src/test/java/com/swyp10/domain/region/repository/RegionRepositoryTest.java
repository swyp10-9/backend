package com.swyp10.domain.region.repository;

import com.swyp10.domain.region.entity.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RegionRepository 테스트")
class RegionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RegionRepository regionRepository;

    private Region region;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .regionCode(1)
            .regionName("서울특별시")
            .parentCode("00")
            .build();
    }

    @Test
    @DisplayName("Region 저장 및 조회")
    void saveAndFindById() {
        // given
        Region saved = regionRepository.save(region);
        entityManager.flush();

        // when
        Optional<Region> found = regionRepository.findById(1);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getRegionName()).isEqualTo("서울특별시");
        assertThat(found.get().getParentCode()).isEqualTo("00");
    }

    @Test
    @DisplayName("Region 삭제")
    void deleteRegion() {
        // given
        Region saved = regionRepository.save(region);
        entityManager.flush();

        // when
        regionRepository.deleteById(saved.getRegionCode());
        entityManager.flush();

        // then
        assertThat(regionRepository.findById(saved.getRegionCode())).isNotPresent();
    }
}
