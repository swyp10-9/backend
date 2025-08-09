package com.swyp10.domain.restaurant.service;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.restaurant.dto.request.FestivalRestaurantPageRequest;
import com.swyp10.domain.restaurant.dto.response.FestivalRestaurantListResponse;
import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailInfo2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailIntro2RestaurantDto;
import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.domain.restaurant.entity.RestaurantBasicInfo;
import com.swyp10.domain.restaurant.repository.RestaurantRepository;
import com.swyp10.exception.ApplicationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@EntityScan(basePackages = "com.swyp10.domain")
@Transactional
@DisplayName("RestaurantService 테스트")
public class RestaurantServiceTest {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    // === 성공 테스트 ===

    @Test
    @DisplayName("레스토랑 저장 및 조회 - 성공")
    void saveAndFindRestaurant_success() {
        AreaBasedList2RestaurantDto searchDto = AreaBasedList2RestaurantDto.builder()
            .contentId("TEST-001")
            .title("테스트 레스토랑")
            .addr1("서울시 테스트구")
            .build();

        DetailIntro2RestaurantDto introDto = DetailIntro2RestaurantDto.builder()
            .chkcreditcardfood("가능")
            .build();

        DetailInfo2RestaurantDto menuDto = DetailInfo2RestaurantDto.builder()
            .serialnum("1")
            .menuname("테스트 메뉴")
            .menuprice("10000")
            .build();

        Restaurant saved = restaurantService.saveOrUpdateRestaurant(searchDto, introDto, List.of(menuDto));
        assertThat(saved.getContentId()).isEqualTo("TEST-001");

        Restaurant found = restaurantService.findByContentId("TEST-001");
        assertThat(found).isNotNull();
        assertThat(found.getBasicInfo().getTitle()).isEqualTo("테스트 레스토랑");
        assertThat(found.getMenus()).hasSize(1);
        assertThat(found.getMenus().get(0).getMenuname()).isEqualTo("테스트 메뉴");
    }

    // === 실패 테스트 ===

    @Test
    @DisplayName("존재하지 않는 contentId 조회시 ApplicationException 발생 - 실패")
    void findByContentId_notFound_fail() {
        assertThatThrownBy(() -> restaurantService.findByContentId("NON_EXISTENT_ID"))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining("식당을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("축제 주변 맛집 조회 - 반경/거리정렬 필터 성공")
    void getFestivalRestaurants_success() {
        // given: 축제(중심 좌표/지역)
        Festival festival = createFestival(37.5, 127.0, 1, "테스트축제");

        // 같은 지역 맛집 3개(2개는 근처, 1개는 멀리)
        restaurantRepository.saveAll(List.of(
            createRestaurant("1111", "근처1", 1, "한식", 37.5005, 127.0005, 4.0),
            createRestaurant("2222", "근처2", 1, "한식", 37.5015, 127.0015, 4.3),
            createRestaurant("3333", "멀리1", 1, "한식", 37.5300, 127.0300, 4.9)
        ));

        FestivalRestaurantPageRequest req = FestivalRestaurantPageRequest.builder()
            .festivalId(festival.getFestivalId())
            .page(0).size(10)
            .radius(1000)                // 반경 1km
            .category("한식")            // 카테고리 필터
            .sort("distance,asc")        // 거리 오름차순
            .build();

        // when
        FestivalRestaurantListResponse res = restaurantService.getFestivalRestaurants(req);

        // then: 2개만 반경 내
        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getContent()).extracting("name")
            .containsExactly("근처1", "근처2");
    }

    @Test
    @DisplayName("축제가 존재하지 않으면 ApplicationException 발생")
    void getFestivalRestaurants_festivalNotFound() {
        // given
        FestivalRestaurantPageRequest req = FestivalRestaurantPageRequest.builder()
            .festivalId(999999L)
            .page(0).size(10)
            .build();

        // expect
        assertThatThrownBy(() -> restaurantService.getFestivalRestaurants(req))
            .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("카테고리/정렬 없이 반경만 요청 - 정상 동작(반경 내 전부 반환)")
    void getFestivalRestaurants_onlyRadius() {
        // given
        Festival festival = createFestival(37.5, 127.0, 1, "테스트축제3");

        restaurantRepository.saveAll(List.of(
            createRestaurant("1111", "근처1", 1, "카페", 37.5005, 127.0005, 4.1),
            createRestaurant("2222", "근처2", 1, "양식", 37.5007, 127.0007, 4.2),
            createRestaurant("3333", "멀리",   1, "한식", 37.5300, 127.0300, 4.9)
        ));

        FestivalRestaurantPageRequest req = FestivalRestaurantPageRequest.builder()
            .festivalId(festival.getFestivalId())
            .page(0).size(10)
            .radius(800)                 // 800m
            .sort("distance,asc")
            .build();

        // when
        FestivalRestaurantListResponse res = restaurantService.getFestivalRestaurants(req);

        // then: 2개
        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getContent()).extracting("name")
            .containsExactly("근처1", "근처2");
    }

    @Test
    @DisplayName("조건에 맞는 맛집 없음 - 빈 페이지")
    void getFestivalRestaurants_empty() {
        // given
        Festival festival = createFestival(37.5, 127.0, 1, "테스트축제4");
        restaurantRepository.saveAll(List.of(
            createRestaurant("1111", "타지역", 2, "한식", 37.5, 127.0, 3.0)
        ));

        FestivalRestaurantPageRequest req = FestivalRestaurantPageRequest.builder()
            .festivalId(festival.getFestivalId())
            .page(0).size(10)
            .category("양식")  // 해당 지역에 양식 없음
            .build();

        // when
        FestivalRestaurantListResponse res = restaurantService.getFestivalRestaurants(req);

        // then
        assertThat(res.getTotalElements()).isZero();
        assertThat(res.getContent()).isEmpty();
    }

    private Festival createFestival(double lat, double lng, int area, String title) {
        FestivalBasicInfo basic = FestivalBasicInfo.builder()
            .title(title)
            .areacode(String.valueOf(area))
            .mapy(lat)   // 위도
            .mapx(lng)   // 경도
            .build();

        Festival festival = Festival.builder()
            .contentId("131313")
            .basicInfo(basic)
            .build();
        return festivalRepository.save(festival);
    }

    private Restaurant createRestaurant(String restaurantId, String name, int area, String category, double lat, double lng, double rating) {
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
}
