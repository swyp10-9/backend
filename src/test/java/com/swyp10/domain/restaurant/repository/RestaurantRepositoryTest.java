package com.swyp10.domain.restaurant.repository;

import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.global.vo.Location;
import com.swyp10.global.vo.What3Words;
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
@DisplayName("RestaurantRepository 테스트")
class RestaurantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
            .name("맛집")
            .category("한식")
            .location(new Location(37.1234, 127.5678, new What3Words("xxx", "yyy", "zzz")))
            .naverPlaceId("12345")
            .phone("010-1234-5678")
            .priceRange("₩₩")
            .imageUrl("http://example.com/image.jpg")
            .build();
    }

    @Test
    @DisplayName("Restaurant 저장 및 조회")
    void saveAndFindById() {
        // given
        Restaurant saved = restaurantRepository.save(restaurant);
        entityManager.flush();

        // when
        Optional<Restaurant> found = restaurantRepository.findById(saved.getRestaurantId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("맛집");
    }

    @Test
    @DisplayName("Restaurant 삭제")
    void deleteRestaurant() {
        // given
        Restaurant saved = restaurantRepository.save(restaurant);
        entityManager.flush();

        // when
        restaurantRepository.deleteById(saved.getRestaurantId());
        entityManager.flush();

        // then
        assertThat(restaurantRepository.findById(saved.getRestaurantId())).isNotPresent();
    }
}
