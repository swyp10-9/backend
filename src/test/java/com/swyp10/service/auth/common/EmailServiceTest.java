package com.swyp10.service.auth.common;

import com.swyp10.domain.auth.service.common.EmailService;
import com.swyp10.domain.auth.service.common.TokenService;
import com.swyp10.domain.auth.service.common.UserService;
import com.swyp10.domain.auth.dto.common.LoginRequest;
import com.swyp10.domain.auth.dto.common.SignupRequest;
import com.swyp10.domain.auth.dto.common.TokenResponse;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService 테스트")
class EmailServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmailService emailService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signupRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("password123")
                .nickname("테스트사용자")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트사용자")
                .password("encodedPassword")
                .signupCompleted(true)
                .build();
    }

    @Test
    @DisplayName("이메일 회원가입 - 성공")
    void signup_Success() {
        // given
        String accessToken = "access-token";
        when(userService.createEmailUser(signupRequest)).thenReturn(user);
        when(tokenService.generateAccessToken(user)).thenReturn(accessToken);

        // when
        TokenResponse result = emailService.signup(signupRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("테스트사용자");

        verify(userService).createEmailUser(signupRequest);
        verify(tokenService).generateAccessToken(user);
    }

    @Test
    @DisplayName("이메일 로그인 - 성공")
    void login_Success() {
        // given
        String accessToken = "access-token";
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(tokenService.generateAccessToken(user)).thenReturn(accessToken);

        // when
        TokenResponse result = emailService.login(loginRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("테스트사용자");

        verify(userService).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(tokenService).generateAccessToken(user);
    }

    @Test
    @DisplayName("이메일 로그인 - 비밀번호 불일치")
    void login_InvalidPassword_ThrowsException() {
        // given
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> emailService.login(loginRequest))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.INVALID_PASSWORD.getMessage());

        verify(userService).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(tokenService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("이메일 중복 확인 - 사용 가능한 이메일")
    void isEmailAvailable_Available() {
        // given
        when(userService.isEmailAvailable("test@example.com")).thenReturn(true);

        // when
        boolean result = emailService.isEmailAvailable("test@example.com");

        // then
        assertThat(result).isTrue();
        verify(userService).isEmailAvailable("test@example.com");
    }

    @Test
    @DisplayName("이메일 중복 확인 - 사용 불가능한 이메일")
    void isEmailAvailable_NotAvailable() {
        // given
        when(userService.isEmailAvailable("test@example.com")).thenReturn(false);

        // when
        boolean result = emailService.isEmailAvailable("test@example.com");

        // then
        assertThat(result).isFalse();
        verify(userService).isEmailAvailable("test@example.com");
    }
}
