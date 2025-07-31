package com.swyp10.domain.review.dto.response;

import com.swyp10.global.page.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "축제 리뷰 목록 응답")
public class FestivalReviewListResponse extends PageResponse<FestivalReviewResponse> {
}
