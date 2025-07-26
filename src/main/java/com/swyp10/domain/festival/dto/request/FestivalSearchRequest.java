package com.swyp10.domain.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FestivalSearchRequest extends FestivalPageRequest {
    @Schema(description = "검색어", example = "음악 축제")
    private String searchParam;
}
