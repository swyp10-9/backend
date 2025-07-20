package com.swyp10.domain.restaurant.service;

import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.domain.restaurant.repository.RestaurantRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.global.vo.Location;
import com.swyp10.global.vo.What3Words;
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
@DisplayName("RestaurantService 테스트")
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
            .restaurantId(1L)
            .name("맛집")
            .category("한식")
            .location(new Location(37.1234, 127.5678, new What3Words("xxx", "yyy", "zzz")))
            .naverPlaceId("12345")
            .phone("010-1234-5678")
            .priceRange("₩₩")
            .imageUrl("http://example.com/image.jpg")
            .build();
    }

    @Nested
    @DisplayName("Restaurant 조회")
    class GetRestaurant {

        @Test
        @DisplayName("ID로 Restaurant 조회 성공")
        void getRestaurantSuccess() {
            // given
            given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

            // when
            Restaurant found = restaurantService.getRestaurant(1L);

            // then
            assertThat(found).isEqualTo(restaurant);
            verify(restaurantRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
        void getRestaurantFail() {
            // given
            given(restaurantRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> restaurantService.getRestaurant(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Restaurant 생성")
    class CreateRestaurant {

        @Test
        @DisplayName("Restaurant 생성 성공")
        void createSuccess() {
            // given
            given(restaurantRepository.save(restaurant)).willReturn(restaurant);

            // when
            Restaurant saved = restaurantService.createRestaurant(restaurant);

            // then
            assertThat(saved).isEqualTo(restaurant);
            verify(restaurantRepository).save(restaurant);
        }
    }

    @Nested
    @DisplayName("Restaurant 삭제")
    class DeleteRestaurant {

        @Test
        @DisplayName("Restaurant 삭제 성공")
        void deleteSuccess() {
            // when
            restaurantService.deleteRestaurant(1L);

            // then
            verify(restaurantRepository).deleteById(1L);
        }
    }
}
