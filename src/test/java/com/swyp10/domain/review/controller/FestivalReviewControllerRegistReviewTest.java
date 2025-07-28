package com.swyp10.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.review.dto.request.FestivalReviewCreateRequest;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FestivalReviewController.class)
@AutoConfigureMockMvc
@DisplayName("FestivalReviewController - 리뷰 등록 테스트")
class FestivalReviewControllerRegistReviewTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserReviewService reviewService;

    @Nested
    @DisplayName("축제 리뷰 등록 API")
    class CreateFestivalReview {

        @Test
        @DisplayName("회원이 리뷰 작성 - 성공")
        void createFestivalReview_success() throws Exception {
            // given
            FestivalReviewCreateRequest req = new FestivalReviewCreateRequest();
            req.setContent("정말 즐거웠어요!");

            when(reviewService.createFestivalReview(any(), any(), any(FestivalReviewCreateRequest.class)))
                .thenReturn(101L);

            // when & then
            mockMvc.perform(post("/api/v1/festivals/{festivalId}/reviews", 1L)
                    .with(csrf()) // PATCH, POST, DELETE엔 CSRF 필요
                    .with(user("홍길동").roles("USER")) // 회원 인증
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(101L)); // Long id 반환
        }

        @Test
        @DisplayName("미인증 사용자가 리뷰 작성 시 401 반환 - 실패")
        void createFestivalReview_unauthenticated() throws Exception {
            // given
            FestivalReviewCreateRequest req = new FestivalReviewCreateRequest();
            req.setContent("비로그인 작성 불가");

            // when & then
            mockMvc.perform(post("/api/v1/festivals/{festivalId}/reviews", 1L)
                    .with(csrf())
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("리뷰 내용이 누락된 경우(검증 에러) - 실패")
        void createFestivalReview_validationFail() throws Exception {
            // given
            FestivalReviewCreateRequest req = new FestivalReviewCreateRequest();
            req.setContent(""); // 빈 값

            // when & then
            mockMvc.perform(post("/api/v1/festivals/{festivalId}/reviews", 1L)
                    .with(csrf())
                    .with(user("홍길동").roles("USER"))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.errorDetail.content").exists());
        }
    }
}
