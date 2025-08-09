package com.swyp10.domain.restaurant.repository;

import com.swyp10.domain.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantCustomRepository {
    Optional<Restaurant> findByContentId(String contentId);
}
