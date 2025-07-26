package com.swyp10.domain.restaurant.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FestivalRestaurantListResponse {
    @Schema(description = "주변 맛집 목록")
    private List<FestivalRestaurantResponse> restaurants;
}
