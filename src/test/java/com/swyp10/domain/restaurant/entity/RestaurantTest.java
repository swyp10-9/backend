package com.swyp10.domain.restaurant.entity;

import com.swyp10.global.vo.Location;
import com.swyp10.global.vo.What3Words;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Restaurant Entity 테스트")
class RestaurantTest {

    @Test
    @DisplayName("Restaurant 엔티티 생성")
    void createRestaurant() {
        Location location = new Location(37.5665, 126.9780, new What3Words("xxx", "yyy", "zzz")); // 서울시청

        Restaurant restaurant = Restaurant.builder()
            .name("을지로 곱창골목")
            .category("한식")
            .location(location)
            .naverPlaceId("naver123")
            .phone("02-123-4567")
            .priceRange("1만원~2만원")
            .imageUrl("https://cdn.example.com/food.jpg")
            .build();

        assertThat(restaurant.getName()).isEqualTo("을지로 곱창골목");
        assertThat(restaurant.getLocation().getMapx()).isEqualTo(37.5665);
    }

    @Test
    @DisplayName("Restaurant 엔티티 수정")
    void updateRestaurant() {
        Restaurant restaurant = Restaurant.builder()
            .name("임시 식당")
            .category("기타")
            .location(new Location(0.0, 0.0, new What3Words("xxx", "yyy", "zzz")))
            .build();

        Location newLocation = new Location(35.1796, 129.0756, new What3Words("aaa", "bbb", "ccc")); // 부산

        restaurant.updateInfo(
            "부산 밀면 맛집",
            "분식",
            newLocation,
            "051-789-1234",
            "만원 이하",
            "https://img.new.com/milmyeon.jpg"
        );

        assertThat(restaurant.getName()).isEqualTo("부산 밀면 맛집");
        assertThat(restaurant.getLocation().getMapx()).isEqualTo(35.1796);
        assertThat(restaurant.getCategory()).isEqualTo("분식");
    }
}
