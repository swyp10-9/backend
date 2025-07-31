package com.swyp10.domain.restaurant.controller;

import com.swyp10.domain.restaurant.dto.request.FestivalRestaurantPageRequest;
import com.swyp10.domain.restaurant.dto.response.FestivalRestaurantListResponse;
import com.swyp10.domain.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
@Tag(name = "맛집", description = "맛집 조회 API")
public class FestivalRestaurantController {

    private final RestaurantService restaurantService;

    @Operation(summary = "맛집 조회", description = "해당 축제 지역 내 맛집 조회 (페이징 지원)")
    @GetMapping("/{festivalId}/restaurants")
    public FestivalRestaurantListResponse getFestivalRestaurants(
            @PathVariable Long festivalId,
            @ModelAttribute FestivalRestaurantPageRequest request) {
        // festivalId를 request에 설정
        request.setFestivalId(festivalId);
        return restaurantService.getFestivalRestaurants(request);
    }
}
