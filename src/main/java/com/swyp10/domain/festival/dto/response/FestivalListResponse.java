package com.swyp10.domain.festival.dto.response;

import com.swyp10.global.page.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "축제 목록 응답")
public class FestivalListResponse extends PageResponse<FestivalSummaryResponse> {
    // 추가적인 필드가 필요한 경우 여기에 추가 
}
