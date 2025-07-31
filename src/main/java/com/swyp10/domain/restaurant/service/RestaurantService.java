package com.swyp10.domain.restaurant.service;

import com.swyp10.domain.restaurant.dto.request.FestivalRestaurantPageRequest;
import com.swyp10.domain.restaurant.dto.response.FestivalRestaurantListResponse;
import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.domain.restaurant.repository.RestaurantRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "Restaurant not found: " + restaurantId));
    }

    @Transactional
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public void deleteRestaurant(Long restaurantId) {
        restaurantRepository.deleteById(restaurantId);
    }

    public FestivalRestaurantListResponse getFestivalRestaurants(FestivalRestaurantPageRequest request) {
        return null;
    }
}
