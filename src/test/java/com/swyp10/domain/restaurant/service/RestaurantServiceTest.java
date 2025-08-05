package com.swyp10.domain.restaurant.service;

import com.swyp10.config.TestConfig;
import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailInfo2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailIntro2RestaurantDto;
import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("RestaurantService 테스트")
public class RestaurantServiceTest {

    @Autowired
    private RestaurantService restaurantService;

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
}
