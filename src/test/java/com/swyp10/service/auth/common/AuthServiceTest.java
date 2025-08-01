package com.swyp10.service.auth.common;

import com.swyp10.domain.auth.dto.common.TokenResponse;
import com.swyp10.domain.auth.entity.LoginType;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.service.common.AuthService;
import com.swyp10.domain.auth.service.common.UserService;
import com.swyp10.domain.auth.service.common.TokenService;
import com.swyp10.domain.auth.service.common.AccountService;
import com.swyp10.domain.auth.service.common.EmailService;
import com.swyp10.domain.auth.service.common.OAuthClientFactory;
import com.swyp10.domain.auth.service.common.OAuthClient;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * AuthService 단위 테스트
 *
 * 통합 인증 서비스의 핵심 기능들을 단위 테스트로 검증합니다.
 * 모든 의존성은 Mock으로 처리됩니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock private OAuthClient mockOAuthClient;
    @Mock private OAuthClientFactory oAuthClientFactory;
    @Mock private EmailService emailService;
    @Mock private UserService userService;
    @Mock private AccountService accountService;
    @Mock private TokenService tokenService;

    @InjectMocks private AuthService authService;

    private User testUser;
    private OAuthAccount testOAuthAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .signupCompleted(true)
                .build();

        testOAuthAccount = OAuthAccount.builder()
                .oauthId(1L)
                .provider(LoginType.KAKAO)
                .providerUserId("12345")
                .providerNickname("OAuth유저")
                .build();
    }

    @Nested
    @DisplayName("토큰 연장 테스트")
    class TokenRefreshTest {

        @Test
        @DisplayName("USER 토큰 연장 성공")
        void refresh_user_token_success() {
            // given
            String authHeader = "Bearer user_token";
            String newToken = "new_user_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn("user_token");
            given(tokenService.refreshAccessToken("user_token")).willReturn(newToken);
            given(tokenService.getTokenType(newToken)).willReturn("USER");
            given(tokenService.getUserIdFromToken(newToken)).willReturn(1L);
            given(userService.findById(1L)).willReturn(testUser);

            // when
            TokenResponse result = authService.refreshToken(authHeader);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(newToken);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getNickname()).isEqualTo("테스트유저");
            assertThat(result.getNeedsAdditionalSignup()).isFalse();

            verify(tokenService).extractAndValidateToken(authHeader);
            verify(tokenService).refreshAccessToken("user_token");
            verify(tokenService).getTokenType(newToken);
            verify(tokenService).getUserIdFromToken(newToken);
            verify(userService).findById(1L);
        }

        @Test
        @DisplayName("OAUTH 토큰 연장 성공")
        void refresh_oauth_token_success() {
            // given
            String authHeader = "Bearer oauth_token";
            String newToken = "new_oauth_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn("oauth_token");
            given(tokenService.refreshAccessToken("oauth_token")).willReturn(newToken);
            given(tokenService.getTokenType(newToken)).willReturn("OAUTH");
            given(tokenService.getOAuthAccountIdFromToken(newToken)).willReturn(1L);
            given(accountService.findById(1L)).willReturn(testOAuthAccount);

            // when
            TokenResponse result = authService.refreshToken(authHeader);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(newToken);
            assertThat(result.getUserId()).isNull(); // OAUTH 토큰은 userId 없음
            assertThat(result.getNickname()).isEqualTo("OAuth유저");
            assertThat(result.getNeedsAdditionalSignup()).isTrue();

            verify(tokenService).extractAndValidateToken(authHeader);
            verify(tokenService).refreshAccessToken("oauth_token");
            verify(tokenService).getTokenType(newToken);
            verify(tokenService).getOAuthAccountIdFromToken(newToken);
            verify(accountService).findById(1L);
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 연장 시 예외 발생")
        void refresh_token_with_invalid_token_throws_exception() {
            // given
            String authHeader = "Bearer invalid_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(authHeader))
                    .isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("알 수 없는 토큰 타입으로 연장 시 예외 발생")
        void refresh_token_with_unknown_type_throws_exception() {
            // given
            String authHeader = "Bearer unknown_token";
            String newToken = "new_unknown_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn("unknown_token");
            given(tokenService.refreshAccessToken("unknown_token")).willReturn(newToken);
            given(tokenService.getTokenType(newToken)).willReturn("UNKNOWN");

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(authHeader))
                    .isInstanceOf(ApplicationException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.UNKNOWN_TOKEN_TYPE);
        }
    }
}
