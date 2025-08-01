package com.swyp10.domain.restaurant.dto.request;

import com.swyp10.global.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "축제 주변 맛집 목록 조회 요청")
public class FestivalRestaurantPageRequest extends PageRequest {
    
    @Schema(description = "축제 ID", required = true, nullable = false, example = "1")
    private Long festivalId;
    
    @Schema(description = "검색 반경 (미터)", required = false, nullable = true, example = "1000")
    private Integer radius;
    
    @Schema(description = "음식 카테고리", required = false, nullable = true, example = "한식")
    private String category;
    
    @Schema(description = "정렬 기준 (distance,asc | rating,desc)", required = false, nullable = true, example = "distance,asc")
    private String sort;
}
