package com.swyp10.service.auth.common;

import com.swyp10.dto.auth.OAuthProvider;
import com.swyp10.dto.auth.common.LoginRequest;
import com.swyp10.dto.auth.common.OAuthUserInfo;
import com.swyp10.dto.auth.common.SignupRequest;
import com.swyp10.dto.auth.common.TokenResponse;
import com.swyp10.entity.LoginType;
import com.swyp10.entity.OAuthAccount;
import com.swyp10.entity.User;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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
    private SignupRequest testSignupRequest;

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
            .providerEmail("test@example.com")
            .providerNickname("카카오유저")
            .user(null) // 초기에는 연결된 사용자 없음
            .build();

        testSignupRequest = SignupRequest.builder()
            .email("test@example.com")
            .password("password123!")
            .nickname("테스트유저")
            .build();
    }

    @Nested
    @DisplayName("OAuth 로그인 테스트")
    class OAuthLoginTest {

        @Test
        @DisplayName("카카오 OAuth 로그인 성공 - 기존 회원")
        void kakao_oauth_login_success_existing_user() {
            // given
            String provider = "kakao";
            String code = "test_auth_code";
            String accessToken = "test_access_token";
            String userToken = "user_jwt_token";

            OAuthUserInfo userInfo = OAuthUserInfo.builder()
                .provider(OAuthProvider.KAKAO)
                .oauthId("12345")
                .email("test@example.com")
                .nickname("카카오유저")
                .build();

            // 회원가입이 완료된 OAuth 계정
            OAuthAccount completedAccount = testOAuthAccount.toBuilder()
                .user(testUser)
                .build();

            // Factory를 통해 클라이언트 반환하도록 모킹
            given(oAuthClientFactory.getClient(OAuthProvider.KAKAO)).willReturn(mockOAuthClient);
            given(mockOAuthClient.getAccessToken(code)).willReturn(accessToken);
            given(mockOAuthClient.getUserInfo(accessToken)).willReturn(userInfo);
            given(accountService.findOrCreate(userInfo)).willReturn(completedAccount);
            given(accountService.isSignupCompleted(1L)).willReturn(true);
            given(accountService.findUserByOAuthAccount(1L)).willReturn(testUser);
            given(tokenService.generateAccessToken(testUser)).willReturn(userToken);

            // when
            TokenResponse result = authService.processOAuthLogin(provider, code);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(userToken);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getNickname()).isEqualTo("테스트유저");

            verify(oAuthClientFactory).getClient(OAuthProvider.KAKAO);
            verify(mockOAuthClient).getAccessToken(code);
            verify(mockOAuthClient).getUserInfo(accessToken);
            verify(accountService).findOrCreate(userInfo);
            verify(accountService).isSignupCompleted(1L);
            verify(accountService).findUserByOAuthAccount(1L);
            verify(tokenService).generateAccessToken(testUser);
        }

        @Test
        @DisplayName("카카오 OAuth 로그인 성공 - 추가 회원가입 필요")
        void kakao_oauth_login_success_needs_additional_signup() {
            // given
            String provider = "kakao";
            String code = "test_auth_code";
            String accessToken = "test_access_token";
            String oauthToken = "oauth_jwt_token";

            OAuthUserInfo userInfo = OAuthUserInfo.builder()
                .provider(OAuthProvider.KAKAO)
                .oauthId("12345")
                .email("test@example.com")
                .nickname("카카오유저")
                .build();

            // Factory를 통해 클라이언트 반환하도록 모킹
            given(oAuthClientFactory.getClient(OAuthProvider.KAKAO)).willReturn(mockOAuthClient);
            given(mockOAuthClient.getAccessToken(code)).willReturn(accessToken);
            given(mockOAuthClient.getUserInfo(accessToken)).willReturn(userInfo);
            given(accountService.findOrCreate(userInfo)).willReturn(testOAuthAccount);
            given(accountService.isSignupCompleted(1L)).willReturn(false);
            given(tokenService.generateOAuthToken(testOAuthAccount)).willReturn(oauthToken);

            // when
            TokenResponse result = authService.processOAuthLogin(provider, code);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(oauthToken);
            assertThat(result.getNickname()).isEqualTo("카카오유저");

            verify(accountService).isSignupCompleted(1L);
            verify(tokenService).generateOAuthToken(testOAuthAccount);
            verify(accountService, never()).findUserByOAuthAccount(any());
        }

        @Test
        @DisplayName("지원하지 않는 OAuth 제공자로 요청 시 예외 발생")
        void oauth_login_with_unsupported_provider_throws_exception() {
            // given
            String unsupportedProvider = "naver";
            String code = "test_code";

            // when & then
            assertThatThrownBy(() -> authService.processOAuthLogin(unsupportedProvider, code))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_OAUTH_PROVIDER);
        }

        @Test
        @DisplayName("카카오 토큰 발급 실패 시 예외 발생")
        void oauth_login_fails_when_kakao_token_request_fails() {
            // given
            String provider = "kakao";
            String code = "invalid_code";

            given(oAuthClientFactory.getClient(OAuthProvider.KAKAO)).willReturn(mockOAuthClient);
            given(mockOAuthClient.getAccessToken(code))
                .willThrow(new ApplicationException(ErrorCode.KAKAO_TOKEN_EXCEPTION));

            // when & then
            assertThatThrownBy(() -> authService.processOAuthLogin(provider, code))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.KAKAO_TOKEN_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("추가 회원가입 테스트")
    class AdditionalSignupTest {

        @Test
        @DisplayName("OAuth 추가 회원가입 성공")
        void complete_additional_signup_success() {
            // given
            String authHeader = "Bearer oauth_token";
            String userToken = "user_jwt_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn("oauth_token");
            given(tokenService.getTokenType("oauth_token")).willReturn("OAUTH");
            given(tokenService.getOAuthAccountIdFromToken("oauth_token")).willReturn(1L);
            given(userService.completeOAuthSignup(1L, testSignupRequest)).willReturn(testUser);
            given(tokenService.generateAccessToken(testUser)).willReturn(userToken);

            // when
            TokenResponse result = authService.completeAdditionalSignup(authHeader, testSignupRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(userToken);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getNickname()).isEqualTo("테스트유저");

            verify(tokenService).extractAndValidateToken(authHeader);
            verify(tokenService).getTokenType("oauth_token");
            verify(userService).completeOAuthSignup(1L, testSignupRequest);
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 추가 회원가입 시 예외 발생")
        void complete_additional_signup_with_invalid_token_throws_exception() {
            // given
            String authHeader = "Bearer invalid_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.completeAdditionalSignup(authHeader, testSignupRequest))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("OAuth 토큰이 아닌 토큰으로 추가 회원가입 시 예외 발생")
        void complete_additional_signup_with_non_oauth_token_throws_exception() {
            // given
            String authHeader = "Bearer user_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn("user_token");
            given(tokenService.getTokenType("user_token")).willReturn("USER");

            // when & then
            assertThatThrownBy(() -> authService.completeAdditionalSignup(authHeader, testSignupRequest))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OAUTH_TOKEN_REQUIRED);
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class GetCurrentUserTest {

        @Test
        @DisplayName("유효한 USER 토큰으로 사용자 정보 조회 성공")
        void get_current_user_success_with_valid_user_token() {
            // given
            String authHeader = "Bearer user_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn("user_token");
            given(tokenService.getTokenType("user_token")).willReturn("USER");
            given(tokenService.getUserIdFromToken("user_token")).willReturn(1L);
            given(userService.findById(1L)).willReturn(testUser);

            // when
            AuthService.UserInfo result = authService.getCurrentUser(authHeader);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getNickname()).isEqualTo("테스트유저");

            verify(tokenService).getUserIdFromToken("user_token");
            verify(userService).findById(1L);
        }

        @Test
        @DisplayName("OAuth 토큰으로 사용자 정보 조회 시 추가 회원가입 필요 예외 발생")
        void get_current_user_with_oauth_token_throws_additional_signup_required() {
            // given
            String authHeader = "Bearer oauth_token";

            given(tokenService.extractAndValidateToken(authHeader)).willReturn("oauth_token");
            given(tokenService.getTokenType("oauth_token")).willReturn("OAUTH");

            // when & then
            assertThatThrownBy(() -> authService.getCurrentUser(authHeader))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADDITIONAL_SIGNUP_REQUIRED);

            verify(userService, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("이메일 인증 위임 테스트")
    class EmailAuthDelegationTest {

        @Test
        @DisplayName("이메일 회원가입 성공")
        void email_signup_success() {
            // given
            TokenResponse expectedResponse = TokenResponse.of("access_token", 1L, "테스트유저");
            given(emailService.signup(testSignupRequest)).willReturn(expectedResponse);

            // when
            TokenResponse result = authService.signup(testSignupRequest);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            verify(emailService).signup(testSignupRequest);
        }

        @Test
        @DisplayName("이메일 로그인 성공")
        void email_login_success() {
            // given
            var loginRequest = new LoginRequest("test@example.com", "password123!");
            TokenResponse expectedResponse = TokenResponse.of("access_token", 1L, "테스트유저");
            given(emailService.login(loginRequest)).willReturn(expectedResponse);

            // when
            TokenResponse result = authService.login(loginRequest);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            verify(emailService).login(loginRequest);
        }

        @Test
        @DisplayName("이메일 중복 확인 성공")
        void check_email_available_success() {
            // given
            String email = "test@example.com";
            given(emailService.isEmailAvailable(email)).willReturn(true);

            // when
            boolean result = authService.checkEmailAvailable(email);

            // then
            assertThat(result).isTrue();
            verify(emailService).isEmailAvailable(email);
        }
    }
}
