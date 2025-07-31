package com.swyp10.domain.mypage.controller;

import com.swyp10.domain.mypage.dto.request.MyInfoUpdateRequest;
import com.swyp10.domain.mypage.dto.response.MyInfoResponse;
import com.swyp10.domain.mypage.dto.response.MyReviewListResponse;
import com.swyp10.domain.mypage.dto.response.MyReviewResponse;
import com.swyp10.domain.mypage.service.MyPageService;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyPageController.class)
@DisplayName("MyPageController 테스트")
class MyPageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MyPageService myPageService;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("내 리뷰 목록 조회 - 성공")
    void getMyReviews_success() throws Exception {
        // given
        List<MyReviewResponse> reviews = List.of(
            MyReviewResponse.builder()
                .id(1L)
                .festivalId(10L)
                .festivalTitle("부산 불꽃축제")
                .festivalThumbnail("https://...")
                .content("최고의 축제!")
                .createdAt(LocalDate.now())
                .build()
        );

        MyReviewListResponse mockResponse = MyReviewListResponse.builder()
            .content(reviews)  // totalCount, reviews -> content로 변경
            .page(0)
            .size(20)
            .totalElements(1L)
            .totalPages(1)
            .first(true)
            .last(true)
            .empty(false)
            .build();

        when(myPageService.getMyReviews(any(), any())).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/mypage/reviews")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].festivalTitle").value("부산 불꽃축제"))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("내 리뷰 목록 조회 - 페이징 파라미터와 함께")
    void getMyReviews_withPagination_success() throws Exception {
        // given
        MyReviewListResponse mockResponse = MyReviewListResponse.builder()
            .content(List.of())
            .page(1)
            .size(10)
            .totalElements(0L)
            .totalPages(0)
            .first(false)
            .last(true)
            .empty(true)
            .build();

        when(myPageService.getMyReviews(any(), any())).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/mypage/reviews")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.size").value(10))
            .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("내 리뷰 삭제 - 성공")
    void deleteMyReview_success() throws Exception {
        // when
        doNothing().when(myPageService).deleteMyReview(any(), any());

        // then
        mockMvc.perform(delete("/api/v1/mypage/reviews/{reviewId}", 1L)
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("내 북마크 취소 - 성공")
    void cancelBookmark_success() throws Exception {
        mockMvc.perform(delete("/api/v1/mypage/bookmarks/{festivalId}", 10L)
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("내 정보 변경 - 성공")
    void updateMyInfo_success() throws Exception {
        MyInfoResponse mockResponse = MyInfoResponse.builder()
            .userId(123L)
            .nickname("홍길동")
            .profileImage("https://...")
            .build();

        when(myPageService.updateMyInfo(any(), any(MyInfoUpdateRequest.class)))
            .thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/mypage/me")
                .with(csrf())
                .param("nickname", "홍길동"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nickname").value("홍길동"));
    }

    // ==================== 실패 케이스 ====================

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("내 리뷰 삭제 - 본인 리뷰 아님 예외 발생 - 실패")
    void deleteMyReview_forbidden() throws Exception {
        doThrow(new ApplicationException(ErrorCode.BAD_REQUEST, "삭제 권한이 없습니다."))
            .when(myPageService).deleteMyReview(any(), any());

        mockMvc.perform(delete("/api/v1/mypage/reviews/{reviewId}", 99L)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("삭제 권한이 없습니다."));
    }

    @Test
    @DisplayName("인증 없이 요청시 401 발생 - 실패")
    void getMyReviews_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/mypage/reviews"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("내 정보 변경 중 서버 에러 - 실패")
    void updateMyInfo_internalServerError() throws Exception {
        when(myPageService.updateMyInfo(any(), any(MyInfoUpdateRequest.class)))
            .thenThrow(new RuntimeException("DB 장애"));

        mockMvc.perform(patch("/api/v1/mypage/me")
                .param("nickname", "홍길동")
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }
}
