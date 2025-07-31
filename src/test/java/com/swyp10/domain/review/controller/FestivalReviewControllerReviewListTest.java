package com.swyp10.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.review.dto.response.FestivalReviewListResponse;
import com.swyp10.domain.review.dto.response.FestivalReviewResponse;
import com.swyp10.domain.review.service.UserReviewService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FestivalReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FestivalReviewController - 리뷰 조회 테스트")
class FestivalReviewControllerReviewListTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserReviewService reviewService;

    @Nested
    @DisplayName("축제 리뷰 목록 조회 API")
    class GetFestivalReviews {

        @Test
        @DisplayName("축제 리뷰 리스트 반환(비회원 접근 가능) - 성공")
        void getFestivalReviews_success() throws Exception {
            // given
            List<FestivalReviewResponse> reviews = List.of(
                FestivalReviewResponse.builder()
                    .id(11L)
                    .nickname("홍길동")
                    .profileImage("https://profile.com/123.jpg")
                    .content("재밌었어요!")
                    .createdAt(LocalDate.of(2025, 8, 1))
                    .build()
            );

            FestivalReviewListResponse mockResponse = FestivalReviewListResponse.builder()
                .content(reviews)  // totalCount, reviews -> content로 변경
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .build();

            when(reviewService.getFestivalReviews(anyLong(), any()))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/reviews", 1L)
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1L))
                .andExpect(jsonPath("$.data.content[0].nickname").value("홍길동"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20));
        }

        @Test
        @DisplayName("페이징 파라미터와 함께 조회 - 성공")
        void getFestivalReviews_withPagination_success() throws Exception {
            // given
            FestivalReviewListResponse mockResponse = FestivalReviewListResponse.builder()
                .content(List.of())
                .page(1)
                .size(10)
                .totalElements(0L)
                .totalPages(0)
                .first(false)
                .last(true)
                .empty(true)
                .build();

            when(reviewService.getFestivalReviews(anyLong(), any()))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/reviews", 1L)
                    .param("page", "1")
                    .param("size", "10")
                    .param("minRating", "3")
                    .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.empty").value(true));
        }

        @Test
        @DisplayName("존재하지 않는 축제 ID - 실패")
        void getFestivalReviews_notFound() throws Exception {
            // given
            when(reviewService.getFestivalReviews(anyLong(), any()))
                .thenThrow(new ApplicationException(ErrorCode.BAD_REQUEST, "축제를 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/reviews", 9999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("축제를 찾을 수 없습니다."));
        }
    }
}
