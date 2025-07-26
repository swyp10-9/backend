package com.swyp10.domain.search.controller;

import com.swyp10.domain.search.dto.response.SearchKeywordListResponse;
import com.swyp10.domain.search.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchKeywordController {

    private final SearchKeywordService searchKeywordService;

    // 인기 검색어 top 10 조회
    @GetMapping("/keywords/top")
    public SearchKeywordListResponse getTopKeywords(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return searchKeywordService.getTopKeywords(limit);
    }
}
