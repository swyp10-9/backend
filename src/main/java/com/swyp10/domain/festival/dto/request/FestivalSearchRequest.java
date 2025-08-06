package com.swyp10.domain.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FestivalSearchRequest extends FestivalPageRequest {
    @Schema(description = "검색 키워드 (축제명, 설명, 지역명 등 자유 검색)", required = false, nullable = true, example = "벚꽃축제")
    private String searchParam;
}
