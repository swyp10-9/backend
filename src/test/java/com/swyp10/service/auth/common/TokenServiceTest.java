package com.swyp10.service.auth.common;

import com.swyp10.constants.TokenType;
import com.swyp10.domain.auth.service.common.TokenService;
import com.swyp10.domain.auth.entity.LoginType;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

/**
 * TokenService 통합 테스트
 * 
 * JWT 토큰의 생성, 검증, 정보 추출 기능을 실제 환경과 유사하게 테스트합니다.
 */
@SpringBootTest(classes = TokenService.class)
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-long-enough-for-hmac-256-algorithm-minimum-32-characters",
    "jwt.expiration=3600"
})
@DisplayName("TokenService 통합 테스트")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

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
            .providerEmail("oauth@example.com")
            .providerNickname("OAuth유저")
            .build();
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class TokenGenerationTest {

        @Test
        @DisplayName("User Access Token 생성 성공")
        void generate_user_access_token_success() {
            // when
            String token = tokenService.generateAccessToken(testUser);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT 형태 확인

            // 토큰 내용 검증
            Claims claims = tokenService.getClaimsFromToken(token);
            assertThat(claims.getSubject()).isEqualTo("1");
            assertThat(claims.get("email")).isEqualTo("test@example.com");
            assertThat(claims.get("nickname")).isEqualTo("테스트유저");
            assertThat(claims.get("tokenType")).isEqualTo("USER");
            assertThat(claims.get("signupCompleted")).isEqualTo(true);
        }

        @Test
        @DisplayName("OAuth Token 생성 성공")
        void generate_oauth_token_success() {
            // when
            String token = tokenService.generateOAuthToken(testOAuthAccount);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);

            // 토큰 내용 검증
            Claims claims = tokenService.getClaimsFromToken(token);
            assertThat(claims.getSubject()).isEqualTo("1");
            assertThat(claims.get("provider")).isEqualTo("KAKAO");
            assertThat(claims.get("providerUserId")).isEqualTo("12345");
            assertThat(claims.get("providerEmail")).isEqualTo("oauth@example.com");
            assertThat(claims.get("tokenType")).isEqualTo("OAUTH");
            assertThat(claims.get("signupCompleted")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class TokenValidationTest {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void validate_valid_token_success() {
            // given
            String token = tokenService.generateAccessToken(testUser);

            // when
            boolean isValid = tokenService.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 토큰 검증 실패")
        void validate_invalid_token_failure() {
            // given
            String invalidToken = "invalid.jwt.token";

            assertThrows(ApplicationException.class, () -> {
                tokenService.validateToken(invalidToken);
            });
        }

        @Test
        @DisplayName("Authorization 헤더에서 토큰 추출 및 검증 성공")
        void extract_and_validate_token_from_header_success() {
            // given
            String token = tokenService.generateAccessToken(testUser);
            String authHeader = "Bearer " + token;

            // when
            String extractedToken = tokenService.extractAndValidateToken(authHeader);

            // then
            assertThat(extractedToken).isEqualTo(token);
        }

        @Test
        @DisplayName("잘못된 Authorization 헤더 형식으로 토큰 추출 실패 - 예외 발생 확인")
        void extract_token_from_invalid_header_failure() {
            // given
            String invalidHeader = "InvalidPrefix token";

            // when & then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                tokenService.extractAndValidateToken(invalidHeader);
            });

            // 예외 메시지 검증
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_REQUEST_HEADER);
        }

    }

    @Nested
    @DisplayName("토큰 정보 추출 테스트")
    class TokenExtractionTest {

        @Test
        @DisplayName("USER 토큰에서 사용자 ID 추출 성공")
        void extract_user_id_from_user_token_success() {
            // given
            String userToken = tokenService.generateAccessToken(testUser);

            // when
            Long userId = tokenService.getUserIdFromToken(userToken);

            // then
            assertThat(userId).isEqualTo(1L);
        }

        @Test
        @DisplayName("OAUTH 토큰에서 사용자 ID 추출 시 예외 발생")
        void extract_user_id_from_oauth_token_throws_exception() {
            // given
            String oauthToken = tokenService.generateOAuthToken(testOAuthAccount);

            // when & then
            assertThatThrownBy(() -> tokenService.getUserIdFromToken(oauthToken))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADDITIONAL_SIGNUP_REQUIRED);
        }

        @Test
        @DisplayName("OAUTH 토큰에서 OAuth 계정 ID 추출 성공")
        void extract_oauth_account_id_from_oauth_token_success() {
            // given
            String oauthToken = tokenService.generateOAuthToken(testOAuthAccount);

            // when
            Long oauthAccountId = tokenService.getOAuthAccountIdFromToken(oauthToken);

            // then
            assertThat(oauthAccountId).isEqualTo(1L);
        }

        @Test
        @DisplayName("USER 토큰에서 OAuth 계정 ID 추출 시 예외 발생")
        void extract_oauth_account_id_from_user_token_throws_exception() {
            // given
            String userToken = tokenService.generateAccessToken(testUser);

            // when & then
            assertThatThrownBy(() -> tokenService.getOAuthAccountIdFromToken(userToken))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OAUTH_TOKEN_REQUIRED);
        }

        @Test
        @DisplayName("토큰 타입 추출 성공")
        void extract_token_type_success() {
            // given
            String userToken = tokenService.generateAccessToken(testUser);
            String oauthToken = tokenService.generateOAuthToken(testOAuthAccount);

            // when
            String userTokenType = tokenService.getTokenType(userToken);
            String oauthTokenType = tokenService.getTokenType(oauthToken);

            // then
            assertThat(userTokenType).isEqualTo(TokenType.USER.getValue());
            assertThat(oauthTokenType).isEqualTo(TokenType.OAUTH.getValue());
        }

        @Test
        @DisplayName("회원가입 완료 여부 확인 성공")
        void check_signup_completion_success() {
            // given
            String userToken = tokenService.generateAccessToken(testUser);
            String oauthToken = tokenService.generateOAuthToken(testOAuthAccount);

            // when
            boolean userSignupCompleted = tokenService.isSignupCompleted(userToken);
            boolean oauthSignupCompleted = tokenService.isSignupCompleted(oauthToken);

            // then
            assertThat(userSignupCompleted).isTrue();
            assertThat(oauthSignupCompleted).isFalse();
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTest {

        @Test
        @DisplayName("잘못된 토큰으로 클레임 추출 시 예외 발생")
        void extract_claims_from_invalid_token_throws_exception() {
            // given
            String invalidToken = "invalid.jwt.token";

            // when & then
            assertThatThrownBy(() -> tokenService.getClaimsFromToken(invalidToken))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_PARSING_FAILED);
        }

        @Test
        @DisplayName("토큰 타입이 없는 토큰으로 타입 추출 시 예외 발생")
        void extract_token_type_from_token_without_type_throws_exception() {
            // given - JWT를 직접 생성하여 tokenType claim이 없는 토큰 만들기
            // 실제로는 우리 시스템에서 생성한 토큰에는 항상 tokenType이 있으므로
            // 외부에서 조작된 토큰을 시뮬레이션
            String tokenWithoutType = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjo5OTk5OTk5OTk5fQ.invalidtoken";

            // when & then
            assertThatThrownBy(() -> tokenService.getTokenType(tokenWithoutType))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_PARSING_FAILED);
        }
    }

    @Nested
    @DisplayName("토큰 연장 테스트")
    class TokenRefreshTest {

        @Test
        @DisplayName("USER 토큰 연장 성공")
        void refresh_user_token_success() throws InterruptedException {
            // given
            String originalToken = tokenService.generateAccessToken(testUser);
            
            // 토큰 생성 시간이 다르도록 잠시 대기
            Thread.sleep(1000);

            // when
            String refreshedToken = tokenService.refreshAccessToken(originalToken);

            // then
            assertThat(refreshedToken).isNotNull();
            assertThat(refreshedToken).isNotEmpty();
            assertThat(refreshedToken).isNotEqualTo(originalToken); // 새로운 토큰이어야 함

            // 기존 토큰과 새 토큰의 내용이 동일한지 확인 (만료시간 제외)
            Claims originalClaims = tokenService.getClaimsFromToken(originalToken);
            Claims refreshedClaims = tokenService.getClaimsFromToken(refreshedToken);

            assertThat(refreshedClaims.getSubject()).isEqualTo(originalClaims.getSubject());
            assertThat(refreshedClaims.get("email")).isEqualTo(originalClaims.get("email"));
            assertThat(refreshedClaims.get("nickname")).isEqualTo(originalClaims.get("nickname"));
            assertThat(refreshedClaims.get("tokenType")).isEqualTo(originalClaims.get("tokenType"));
            assertThat(refreshedClaims.get("signupCompleted")).isEqualTo(originalClaims.get("signupCompleted"));

            // 새 토큰의 발급시간이 더 최신이어야 함
            assertThat(refreshedClaims.getIssuedAt()).isAfter(originalClaims.getIssuedAt());
        }

        @Test
        @DisplayName("OAUTH 토큰 연장 성공")
        void refresh_oauth_token_success() throws InterruptedException {
            // given
            String originalToken = tokenService.generateOAuthToken(testOAuthAccount);
            
            // 토큰 생성 시간이 다르도록 잠시 대기
            Thread.sleep(1000);

            // when
            String refreshedToken = tokenService.refreshAccessToken(originalToken);

            // then
            assertThat(refreshedToken).isNotNull();
            assertThat(refreshedToken).isNotEmpty();
            assertThat(refreshedToken).isNotEqualTo(originalToken);

            // 기존 토큰과 새 토큰의 내용이 동일한지 확인
            Claims originalClaims = tokenService.getClaimsFromToken(originalToken);
            Claims refreshedClaims = tokenService.getClaimsFromToken(refreshedToken);

            assertThat(refreshedClaims.getSubject()).isEqualTo(originalClaims.getSubject());
            assertThat(refreshedClaims.get("provider")).isEqualTo(originalClaims.get("provider"));
            assertThat(refreshedClaims.get("providerUserId")).isEqualTo(originalClaims.get("providerUserId"));
            assertThat(refreshedClaims.get("providerNickname")).isEqualTo(originalClaims.get("providerNickname"));
            assertThat(refreshedClaims.get("providerEmail")).isEqualTo(originalClaims.get("providerEmail"));
            assertThat(refreshedClaims.get("tokenType")).isEqualTo(originalClaims.get("tokenType"));

            // 새 토큰의 발급시간이 더 최신이어야 함
            assertThat(refreshedClaims.getIssuedAt()).isAfter(originalClaims.getIssuedAt());
        }

        @Test
        @DisplayName("유효하지 않은 토큰 연장 시 예외 발생")
        void refresh_invalid_token_throws_exception() {
            // given
            String invalidToken = "invalid.jwt.token";

            // when & then
            assertThatThrownBy(() -> tokenService.refreshAccessToken(invalidToken))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOKEN_PARSING_FAILED);
        }
    }
}
