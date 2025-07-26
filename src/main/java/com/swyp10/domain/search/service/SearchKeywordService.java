package com.swyp10.domain.search.service;

import com.swyp10.domain.search.dto.response.SearchKeywordListResponse;

public interface SearchKeywordService {
    SearchKeywordListResponse getTopKeywords(int limit);
}
