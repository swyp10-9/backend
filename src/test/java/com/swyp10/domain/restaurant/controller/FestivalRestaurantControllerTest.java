package com.swyp10.domain.restaurant.controller;

import com.swyp10.domain.restaurant.dto.request.FestivalRestaurantPageRequest;
import com.swyp10.domain.restaurant.dto.response.FestivalRestaurantListResponse;
import com.swyp10.domain.restaurant.dto.response.FestivalRestaurantResponse;
import com.swyp10.domain.restaurant.service.RestaurantService;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FestivalRestaurantController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FestivalRestaurantController 테스트")
class FestivalRestaurantControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RestaurantService restaurantService;

    @Nested
    @DisplayName("축제 맛집 조회 API")
    class GetFestivalRestaurants {

        @Test
        @DisplayName("맛집 리스트 조회 - 성공")
        void getFestivalRestaurants_success() throws Exception {
            // given
            FestivalRestaurantListResponse mockResponse = FestivalRestaurantListResponse.builder()
                .content(List.of(
                    FestivalRestaurantResponse.builder()
                        .name("해운대 횟집")
                        .address("부산광역시 해운대구 해운대해변로 123")
                        .imageUrl("https://...")
                        .build()
                ))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

            when(restaurantService.getFestivalRestaurants(any(FestivalRestaurantPageRequest.class)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/restaurants", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("해운대 횟집"))
                .andExpect(jsonPath("$.data.content[0].address").value("부산광역시 해운대구 해운대해변로 123"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("축제 ID가 존재하지 않으면 400 에러 반환 - 실패")
        void getFestivalRestaurants_festivalNotFound() throws Exception {
            // given
            when(restaurantService.getFestivalRestaurants(any(FestivalRestaurantPageRequest.class)))
                .thenThrow(new ApplicationException(ErrorCode.BAD_REQUEST, "Festival not found"));

            // when & then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/restaurants", 9999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Festival not found"));
        }
    }
}
