package com.swyp10.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.auth.controller.AuthController;
import com.swyp10.domain.auth.dto.common.SignupRequest;
import com.swyp10.domain.auth.dto.common.TokenResponse;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.exception.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TokenResponse tokenResponse;
    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        tokenResponse = TokenResponse.of("test-access-token", 1L, "테스트사용자");
        signupRequest = new SignupRequest("test@example.com", "password123", "테스트사용자");
    }

    @Test
    @DisplayName("OAuth 로그인 - 성공")
    void oauthLogin_Success() throws Exception {
        // given
        when(authService.processOAuthLogin("kakao", "test-code"))
                .thenReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
                .param("code", "test-code")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.nickname").value("테스트사용자"));
    }

    @Test
    @DisplayName("OAuth 로그인 - 필수 파라미터 누락")
    void oauthLogin_MissingCode() throws Exception {
        // when & then
     mockMvc.perform(post("/api/v1/auth/oauth/login/kakao")
             .contentType(MediaType.APPLICATION_JSON))
             .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 연장 - 성공")
    void refreshToken_Success() throws Exception {
        // given
        TokenResponse refreshResponse = TokenResponse.of("new-access-token", 1L, "테스트사용자");
        
        when(authService.refreshToken("Bearer test-access-token"))
                .thenReturn(refreshResponse);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Authorization", "Bearer test-access-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.nickname").value("테스트사용자"));
    }

    @Test
    @DisplayName("토큰 연장 - Authorization 헤더 누락")
    void refreshToken_MissingAuthHeader() throws Exception {
        // given
        when(authService.refreshToken(null))
                .thenThrow(new ApplicationException(ErrorCode.MISSING_REQUEST_HEADER));

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
