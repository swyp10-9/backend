package com.swyp10.domain.restaurant.repository;

import com.swyp10.domain.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantCustomRepository {

    /**
     * 지역코드(areacode) 기반 + 선택 필터(카테고리, 반경) + 정렬(distance|rating)
     * centerLat/centerLng 가 있을 때 distance 정렬/필터 동작
     */
    Page<Restaurant> findByAreaWithFilters(
        String areaCode,
        String category,
        Integer radiusMeters,        // null 이면 반경 필터 미적용
        Double centerLat,            // null 이면 distance 정렬/필터 미적용
        Double centerLng,
        String sort,                 // "distance,asc" | "rating,desc" | "name,asc" 등
        Pageable pageable
    );
}
