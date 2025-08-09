package com.swyp10.domain.restaurant.repository;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.domain.restaurant.entity.RestaurantBasicInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EntityScan(basePackages = "com.swyp10.domain")
@ActiveProfiles("test")
@Import({TestConfig.class, RestaurantCustomRepositoryImplTest.QuerydslTestConfig.class})
class RestaurantCustomRepositoryImplTest {

    @TestConfiguration
    static class QuerydslTestConfig {
        @PersistenceContext
        EntityManager em;

        @Bean
        com.querydsl.jpa.impl.JPAQueryFactory jpaQueryFactory() {
            return new com.querydsl.jpa.impl.JPAQueryFactory(em);
        }
    }

    @jakarta.annotation.Resource
    RestaurantRepository restaurantRepository;

    private Restaurant r(String restaurantId, String name, int area, String category, double lat, double lng, double rating) {
        RestaurantBasicInfo basicinfo = RestaurantBasicInfo.builder()
            .title(name)
            .areacode(String.valueOf(area))
            .mapy(lat)
            .mapx(lng)
            .addr1("주소-" + name)
            .firstimage("http://img/" + name + ".jpg")
            .build();

        return Restaurant.builder()
            .contentId(restaurantId)
            .basicInfo(basicinfo)
            .build();
    }

    @Test
    @DisplayName("거리 기준 정렬 + 반경 필터(1km) 성공")
    void findByAreaWithFilters_distanceAndRadius() {
        // given: 중심점(37.5,127.0) 기준
        restaurantRepository.saveAll(List.of(
            r("1111", "근처1", 1, "한식", 37.5005, 127.0005, 4.0), // 약 70m 남쪽/동쪽
            r("2222", "근처2", 1, "한식", 37.5015, 127.0015, 4.3), // 약 210m
            r("3333", "멀리1", 1, "한식", 37.5200, 127.0200, 5.0)  // 수 km
        ));

        // when: 반경 1000m, distance asc
        Page<Restaurant> page = restaurantRepository.findByAreaWithFilters(
            "1", "한식", 1000, 37.5, 127.0, "distance,asc", PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(content -> content.getBasicInfo().getTitle())
            .containsExactly("근처1", "근처2");
    }

    @Test
    @DisplayName("distance 정렬 요청 + 중심좌표 없음 → name asc로 fallback")
    void findByAreaWithFilters_distanceWithoutCenter_fallback() {
        // given
        restaurantRepository.saveAll(List.of(
            r("1111", "B", 1, "한식", 37.6, 127.1, 4.0),
            r("2222", "A", 1, "한식", 37.4, 126.9, 4.0)
        ));

        // when
        Page<Restaurant> page = restaurantRepository.findByAreaWithFilters(
            "1", "한식", null, null, null, "distance,asc", PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).extracting(content -> content.getBasicInfo().getTitle())
            .containsExactly("A", "B");
    }

    @Test
    @DisplayName("조건에 맞는 결과 없음 → 빈 페이지")
    void findByAreaWithFilters_empty() {
        // given
        restaurantRepository.save(r("1111", "다른지역", 2, "한식", 37.5, 127.0, 4.0));

        // when
        Page<Restaurant> page = restaurantRepository.findByAreaWithFilters(
            "1", "양식", 500, 37.5, 127.0, "name,asc", PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}
