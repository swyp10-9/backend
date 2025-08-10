package com.swyp10.domain.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.dto.request.*;
import com.swyp10.domain.festival.dto.response.FestivalListResponse;
import com.swyp10.domain.festival.dto.response.FestivalSummaryResponse;
import com.swyp10.domain.festival.service.FestivalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        List<FestivalSummaryResponse> festivals = List.of(
            FestivalSummaryResponse.builder()
                .id(1L)
                .title("부산 불꽃축제")
                .address("부산광역시")
                .startDate(LocalDate.of(2024, 10, 15))
                .endDate(LocalDate.of(2024, 10, 15))
                .thumbnail("https://example.com/thumbnail1.jpg")
                .build()
        );

        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .content(festivals)        // festivals -> content
            .page(0)
            .size(20)
            .totalElements(1L)         // totalCount -> totalElements
            .totalPages(1)
            .first(true)
            .last(true)
            .empty(false)
            .build();

        when(festivalService.getFestivalsForMap(any(Long.class), any(FestivalMapRequest.class)))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/festivals/map")
                .param("latitude", "35.1595")
                .param("longitude", "129.0618")
                .param("radius", "10000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))     // totalCount -> totalElements
            .andExpect(jsonPath("$.data.content[0].title").value("부산 불꽃축제"))  // festivals -> content
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("달력 페이지 축제 리스트 조회 - 성공")
    void getFestivalsForCalendar_success() throws Exception {
        // given
        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .content(List.of())
            .page(0)
            .size(20)
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .empty(true)
            .build();

        when(festivalService.getFestivalsForCalendar(any(Long.class), any(FestivalCalendarRequest.class)))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/festivals/calendar")
                .param("year", "2024")
                .param("month", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    @DisplayName("맞춤 축제 페이지 축제 리스트 조회 - 성공")
    void getFestivalsForPersonalTest_success() throws Exception {
        // given
        List<FestivalSummaryResponse> festivals = List.of(
            FestivalSummaryResponse.builder()
                .id(1L)
                .title("맞춤 축제 1")
                .build(),
            FestivalSummaryResponse.builder()
                .id(2L)
                .title("맞춤 축제 2")
                .build()
        );

        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .content(festivals)
            .page(0)
            .size(20)
            .totalElements(2L)
            .totalPages(1)
            .first(true)
            .last(true)
            .empty(false)
            .build();

        when(festivalService.getFestivalsForPersonalTest(any(Long.class), any(FestivalPersonalTestRequest.class)))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/festivals/personal-test")
                .param("ageGroup", "20대")
                .param("interest", "음악"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content[0].title").value("맞춤 축제 1"));
    }

    @Test
    @DisplayName("검색 페이지 축제 리스트 조회 - 성공")
    void searchFestivals_success() throws Exception {
        // given
        List<FestivalSummaryResponse> festivals = List.of(
            FestivalSummaryResponse.builder()
                .id(1L)
                .title("벚꽃축제")
                .address("서울특별시")
                .build(),
            FestivalSummaryResponse.builder()
                .id(2L)
                .title("진해 벚꽃축제")
                .address("경상남도")
                .build()
        );

        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .content(festivals)
            .page(0)
            .size(20)
            .totalElements(2L)
            .totalPages(1)
            .first(true)
            .last(true)
            .empty(false)
            .build();

        when(festivalService.searchFestivals(any(Long.class), any(FestivalSearchRequest.class)))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/festivals/search")
                .param("keyword", "벚꽃")
                .param("regionCode", "11")
                .param("startDate", "2024-04-01")
                .param("endDate", "2024-04-30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content[0].title").value("벚꽃축제"))
            .andExpect(jsonPath("$.data.content[1].title").value("진해 벚꽃축제"));
    }

    @Test
    @DisplayName("마이페이지 축제 리스트 조회 - 성공")
    void getMyPageFestivals_success() throws Exception {
        // given
        List<FestivalSummaryResponse> festivals = List.of(
            FestivalSummaryResponse.builder()
                .id(1L)
                .title("북마크한 축제 1")
                .build(),
            FestivalSummaryResponse.builder()
                .id(2L)
                .title("북마크한 축제 2")
                .build(),
            FestivalSummaryResponse.builder()
                .id(3L)
                .title("북마크한 축제 3")
                .build()
        );

        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .content(festivals)
            .page(0)
            .size(20)
            .totalElements(3L)
            .totalPages(1)
            .first(true)
            .last(true)
            .empty(false)
            .build();

        when(festivalService.getMyBookmarkedFestivals(eq(1L), any(FestivalMyPageRequest.class)))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/festivals/mypage")
                .param("bookmarked", "true")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content").value(org.hamcrest.Matchers.hasSize(3)))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("페이징 파라미터와 함께 검색 - 성공")
    void searchFestivals_withPagination_success() throws Exception {
        // given
        FestivalListResponse mockResponse = FestivalListResponse.builder()
            .content(List.of())
            .page(1)
            .size(10)
            .totalElements(25L)
            .totalPages(3)
            .first(false)
            .last(false)
            .empty(true)
            .build();

        when(festivalService.searchFestivals(any(Long.class), any(FestivalSearchRequest.class)))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/festivals/search")
                .param("keyword", "축제")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "startDate,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.size").value(10))
            .andExpect(jsonPath("$.data.totalElements").value(25))
            .andExpect(jsonPath("$.data.totalPages").value(3))
            .andExpect(jsonPath("$.data.first").value(false))
            .andExpect(jsonPath("$.data.last").value(false));
    }
}
