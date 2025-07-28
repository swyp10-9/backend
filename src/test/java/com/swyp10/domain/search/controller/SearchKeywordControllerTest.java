package com.swyp10.domain.search.controller;

import com.swyp10.domain.search.dto.response.SearchKeywordListResponse;
import com.swyp10.domain.search.dto.response.SearchKeywordResponse;
import com.swyp10.domain.search.service.SearchKeywordService;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchKeywordController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SearchKeywordController 테스트")
class SearchKeywordControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SearchKeywordService searchKeywordService;

    @Test
    @DisplayName("인기 검색어 Top10 조회 - 성공")
    void getTopKeywords_success() throws Exception {
        // given
        SearchKeywordListResponse mockResponse = SearchKeywordListResponse.builder()
            .keywords(List.of(
                SearchKeywordResponse.builder()
                    .keyword("불꽃축제")
                    .count(321L)
                    .lastSearchedAt(LocalDateTime.of(2025, 8, 1, 10, 0))
                    .build(),
                SearchKeywordResponse.builder()
                    .keyword("음식축제")
                    .count(120L)
                    .lastSearchedAt(LocalDateTime.of(2025, 8, 2, 14, 30))
                    .build()
            ))
            .build();

        when(searchKeywordService.getTopKeywords(anyInt())).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/search/keywords/top")
                .param("limit", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.keywords[0].keyword").value("불꽃축제"))
            .andExpect(jsonPath("$.data.keywords[0].count").value(321))
            .andExpect(jsonPath("$.data.keywords[1].keyword").value("음식축제"))
            .andExpect(jsonPath("$.data.keywords[1].count").value(120));
    }

    @Test
    @DisplayName("검색어 limit이 0 이하인 경우 400 반환 - 실패")
    void getTopKeywords_limitIsZeroOrNegative() throws Exception {
        // given: 서비스가 유효성 예외를 던지는 상황
        when(searchKeywordService.getTopKeywords(0))
            .thenThrow(new ApplicationException(ErrorCode.BAD_REQUEST, "limit은 1 이상이어야 합니다."));

        // when & then
        mockMvc.perform(get("/api/v1/search/keywords/top")
                .param("limit", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("limit은 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("검색어 조회 중 예상치 못한 에러 발생시 500 반환 - 실패")
    void getTopKeywords_internalServerError() throws Exception {
        // given: 서비스가 런타임 예외를 던지는 상황
        when(searchKeywordService.getTopKeywords(anyInt()))
            .thenThrow(new RuntimeException("DB 연결 에러"));

        // when & then
        mockMvc.perform(get("/api/v1/search/keywords/top"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }
}
