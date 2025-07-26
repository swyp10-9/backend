package com.swyp10.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchKeywordListResponse {
    @Schema(description = "인기 검색어 목록")
    private List<SearchKeywordResponse> keywords;
}
