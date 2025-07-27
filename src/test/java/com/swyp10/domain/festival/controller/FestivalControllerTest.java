package com.swyp10.domain.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.service.FestivalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FestivalController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FestivalController 테스트")
class FestivalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FestivalService festivalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("지도 페이지 축제 리스트 조회 - 성공")
    void getFestivalsForMap_success() throws Exception {
        // given
        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .totalCount(1L)
            .offset(0)
            .limit(20)
            .festivals(List.of())
            .build();

        when(festivalService.getFestivalsForMap(any(FestivalMapRequest.class)))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/festivals/map"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCount").value(1));
    }

    @Test
    @DisplayName("달력 페이지 축제 리스트 조회 - 성공")
    void getFestivalsForCalendar_success() throws Exception {
        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .totalCount(0L)
            .offset(0)
            .limit(20)
            .festivals(List.of())
            .build();

        when(festivalService.getFestivalsForCalendar(any(FestivalCalendarRequest.class)))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/festivals/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCount").value(0));
    }

    @Test
    @DisplayName("맞춤 축제 페이지 축제 리스트 조회 - 성공")
    void getFestivalsForPersonalTest_success() throws Exception {
        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .totalCount(2L)
            .offset(0)
            .limit(20)
            .festivals(List.of())
            .build();

        when(festivalService.getFestivalsForPersonalTest(any(FestivalPersonalTestRequest.class)))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/festivals/personal-test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCount").value(2));
    }

    @Test
    @DisplayName("검색 페이지 축제 리스트 조회 - 성공")
    void searchFestivals_success() throws Exception {
        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .totalCount(5L)
            .offset(0)
            .limit(20)
            .festivals(List.of())
            .build();

        when(festivalService.searchFestivals(any(FestivalSearchRequest.class)))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/festivals/search"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCount").value(5));
    }

    @Test
    @DisplayName("마이페이지 축제 리스트 조회 - 성공")
    void getMyPageFestivals_success() throws Exception {
        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .totalCount(3L)
            .offset(0)
            .limit(20)
            .festivals(List.of())
            .build();

        when(festivalService.getMyPageFestivals(any(FestivalMyPageRequest.class)))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/festivals/mypage"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCount").value(3));
    }
}
