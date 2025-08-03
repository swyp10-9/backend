package com.swyp10.domain.restaurant.repository;

import com.swyp10.domain.restaurant.entity.RestaurantMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantMenuRepository extends JpaRepository<RestaurantMenu, Long> {
}
