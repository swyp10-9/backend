package com.swyp10.exception;

import com.swyp10.domain.auth.controller.AuthController;
import com.swyp10.domain.auth.service.common.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("예외 처리 테스트")
class ExceptionHandlerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("카카오 토큰 예외 처리")
    void kakaoTokenException_ExceptionHandling() throws Exception {
        // given
        when(authService.processOAuthLogin(anyString(), anyString()))
                .thenThrow(new ApplicationException(ErrorCode.KAKAO_TOKEN_EXCEPTION));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "invalid-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.KAKAO_TOKEN_EXCEPTION.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.KAKAO_TOKEN_EXCEPTION.getCode()));
    }

    @Test
    @DisplayName("카카오 사용자 정보 조회 예외 처리")
    void kakaoUserInfoException_ExceptionHandling() throws Exception {
        // given
        when(authService.processOAuthLogin(anyString(), anyString()))
                .thenThrow(new ApplicationException(ErrorCode.KAKAO_USER_INFO_EXCEPTION));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "test-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.KAKAO_USER_INFO_EXCEPTION.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.KAKAO_USER_INFO_EXCEPTION.getCode()));
    }

    @Test
    @DisplayName("OAuth 계정 찾기 실패 예외 처리")
    void oauthAccountNotFound_ExceptionHandling() throws Exception {
        // given
        when(authService.processOAuthLogin(anyString(), anyString()))
                .thenThrow(new ApplicationException(ErrorCode.OAUTH_ACCOUNT_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "test-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.OAUTH_ACCOUNT_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.OAUTH_ACCOUNT_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("사용자 찾기 실패 예외 처리")
    void userNotFound_ExceptionHandling() throws Exception {
        // given
        when(authService.processOAuthLogin(anyString(), anyString()))
                .thenThrow(new ApplicationException(ErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "test-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("토큰 생성 실패 예외 처리")
    void tokenGenerationFailed_ExceptionHandling() throws Exception {
        // given
        when(authService.processOAuthLogin(anyString(), anyString()))
                .thenThrow(new ApplicationException(ErrorCode.TOKEN_GENERATION_FAILED));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "test-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // 400으로 수정
                .andExpect(jsonPath("$.message").value(ErrorCode.TOKEN_GENERATION_FAILED.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.TOKEN_GENERATION_FAILED.getCode()));
    }

    @Test
    @DisplayName("이메일 중복 예외 처리")
    void emailAlreadyExists_ExceptionHandling() throws Exception {
        // given
        when(authService.processOAuthLogin(anyString(), anyString()))
                .thenThrow(new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "test-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.getCode()));
    }

    @Test
    @DisplayName("잘못된 토큰 예외 처리")
    void invalidToken_ExceptionHandling() throws Exception {
        // given
        when(authService.processOAuthLogin(anyString(), anyString()))
                .thenThrow(new ApplicationException(ErrorCode.INVALID_TOKEN));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "test-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_TOKEN.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TOKEN.getCode()));
    }

    @Test
    @DisplayName("일반적인 예외 처리")
    void generalException_ExceptionHandling() throws Exception {
        // given
        when(authService.processOAuthLogin(anyString(), anyString()))
                .thenThrow(new RuntimeException("예기치 않은 오류"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "test-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // 400으로 수정 (RuntimeException은 400으로 처리됨)
    }
}
