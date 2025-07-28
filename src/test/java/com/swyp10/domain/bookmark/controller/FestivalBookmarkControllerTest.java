package com.swyp10.domain.bookmark.controller;

import com.swyp10.domain.bookmark.service.UserBookmarkService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FestivalBookmarkController.class)
@AutoConfigureMockMvc
@DisplayName("FestivalBookmarkController 테스트")
class FestivalBookmarkControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserBookmarkService bookmarkService;

    @Nested
    @DisplayName("축제 북마크 저장 API")
    class AddBookmark {

        @Test
        @DisplayName("로그인 사용자가 북마크 저장 - 성공")
        void addBookmark_success() throws Exception {
            // given
            when(bookmarkService.addBookmark(any(), any()))
                .thenReturn(111L);

            // when & then
            mockMvc.perform(post("/api/v1/festivals/{festivalId}/bookmarks", 1001L)
                    .with(csrf()) // POST, PATCH, DELETE에 필수
                    .with(user("홍길동").roles("USER"))) // 로그인 상태 Mock
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(111L));
        }

        @Test
        @DisplayName("미인증 사용자는 401 반환 - 실패")
        void addBookmark_unauthenticated() throws Exception {
            // when & then
            mockMvc.perform(post("/api/v1/festivals/{festivalId}/bookmarks", 1001L)
                    .with(csrf()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("이미 북마크한 축제(비즈니스 예외) - 실패")
        void addBookmark_alreadyExists() throws Exception {
            // given
            when(bookmarkService.addBookmark(any(), any()))
                .thenThrow(new ApplicationException(ErrorCode.USER_ALREADY_EXISTS, "이미 북마크된 축제입니다."));

            // when & then
            mockMvc.perform(post("/api/v1/festivals/{festivalId}/bookmarks", 1001L)
                    .with(csrf())
                    .with(user("홍길동").roles("USER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 북마크된 축제입니다."));
        }
    }
}
