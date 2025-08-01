package com.swyp10.domain.restaurant.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FestivalRestaurantResponse {
    @Schema(description = "식당명", required = false, nullable = false, example = "해운대 횟집")
    private String name;
    @Schema(description = "주소", required = false, nullable = true, example = "부산광역시 해운대구 해운대해변로 123")
    private String address;
    @Schema(description = "대표 이미지", required = false, nullable = true, example = "https://...")
    private String imageUrl;
}
