package com.swyp10.domain.search.controller;

import com.swyp10.domain.search.dto.request.SearchKeywordPageRequest;
import com.swyp10.domain.search.dto.response.SearchKeywordListResponse;
import com.swyp10.domain.search.service.SearchKeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "검색", description = "검색어 순위 & 조회 API")
public class SearchKeywordController {

    private final SearchKeywordService searchKeywordService;

    @Operation(summary = "인기 검색어 조회", description = "인기 검색어 조회 - Top10")
    @GetMapping("/keywords/top")
    public SearchKeywordListResponse getTopKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return searchKeywordService.getTopKeywords(limit);
    }
}
