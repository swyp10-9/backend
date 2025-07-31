package com.swyp10.domain.search.service;

import com.swyp10.domain.search.dto.request.SearchKeywordPageRequest;
import com.swyp10.domain.search.dto.response.SearchKeywordListResponse;
import com.swyp10.domain.search.dto.response.SearchKeywordResponse;
import com.swyp10.domain.search.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchKeywordServiceImpl implements SearchKeywordService {

    private final SearchKeywordRepository searchKeywordRepository;

    @Override
    public SearchKeywordListResponse getTopKeywords(int limit) {
        List<SearchKeywordResponse> keywords = searchKeywordRepository.findTop10ByOrderByCountDescLastSearchedAtDesc()
                .stream()
                .limit(limit)
                .map(sk -> SearchKeywordResponse.builder()
                    .keyword(sk.getKeyword())
                    .count(sk.getCount())
                    .lastSearchedAt(sk.getLastSearchedAt())
                    .build())
                .collect(Collectors.toList());

        return SearchKeywordListResponse.builder()
            .content(keywords)  // keywords -> content로 변경
            .page(0)
            .size(limit)
            .totalElements((long) keywords.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .empty(keywords.isEmpty())
            .build();
    }
}
