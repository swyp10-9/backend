package com.swyp10.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SearchKeywordResponse {
    @Schema(description = "검색어", example = "불꽃축제")
    private String keyword;

    @Schema(description = "검색 횟수", example = "321")
    private Long count;

    @Schema(description = "최근 검색일시", example = "2025-08-01T20:15:30")
    private LocalDateTime lastSearchedAt;
}
