package com.swyp10.domain.review.dto.request;

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
@Schema(description = "축제 리뷰 목록 조회 요청")
public class FestivalReviewPageRequest extends PageRequest {
    
    @Schema(description = "축제 ID", required = true, nullable = false, example = "1")
    private Long festivalId;
    
    @Schema(description = "최소 평점", required = false, nullable = true, example = "3")
    private Integer minRating;
    
    @Schema(description = "최대 평점", required = false, nullable = true, example = "5")
    private Integer maxRating;
    
    @Schema(description = "정렬 기준 (createdAt,desc | rating,desc | likeCount,desc)", required = false, nullable = true, example = "createdAt,desc")
    private String sort;
}
