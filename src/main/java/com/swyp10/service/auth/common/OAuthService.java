package com.swyp10.service.auth.common;

import com.swyp10.dto.auth.OAuthProvider;
import com.swyp10.dto.auth.kakao.KakaoTokenResponse;
import com.swyp10.service.auth.kakao.KakaoOAuthClient;
import com.swyp10.dto.auth.common.OAuthUserInfo;
import com.swyp10.dto.auth.common.TokenResponse;
import com.swyp10.entity.OAuthAccount;
import com.swyp10.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    
    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    
    /**
     * OAuth 인가 코드로 로그인 처리 - 항상 토큰 반환
     */
    public OAuthLoginResult processOAuthLoginByCode(OAuthProvider provider, String authorizationCode) {
        // 1단계: 인가 코드로 액세스 토큰 발급
        String accessToken = getAccessTokenByProvider(provider, authorizationCode);
        
        // 2단계: 액세스 토큰으로 사용자 정보 조회
        OAuthUserInfo oauthUserInfo = getUserInfoByProvider(provider, accessToken);
        
        // 3단계: OAuth 계정 찾기 또는 생성
        OAuthAccount oauthAccount = userService.findOrCreateOAuthAccount(oauthUserInfo);
        
        // 4단계: 회원가입 완료 여부 확인
        boolean isSignupCompleted = userService.isSignupCompleted(oauthAccount.getOauthId());
        
        if (isSignupCompleted) {
            // 회원가입이 완료된 경우: USER 토큰 생성
            User user = userService.findUserByOAuthAccount(oauthAccount.getOauthId());
            String completedAccessToken = jwtTokenService.generateAccessToken(user);
            
            log.info("OAuth 로그인 성공 (회원가입 완료): provider={}, userId={}", 
                    provider, user.getUserId());
            
            return OAuthLoginResult.loginSuccess(
                TokenResponse.of(completedAccessToken, user.getUserId(), user.getNickname())
            );
        } else {
            // 회원가입이 미완료된 경우: OAUTH 토큰 생성
            String oauthToken = jwtTokenService.generateOAuthToken(oauthAccount);
            
            log.info("OAuth 로그인 성공 (추가 회원가입 필요): provider={}, oauthAccountId={}", 
                    provider, oauthAccount.getOauthId());
            
            return OAuthLoginResult.needsAdditionalSignup(
                TokenResponse.ofOAuth(oauthToken, oauthAccount.getProviderNickname())
            );
        }
    }
    
    /**
     * Provider별 액세스 토큰 발급
     */
    private String getAccessTokenByProvider(OAuthProvider provider, String authorizationCode) {
        return switch (provider) {
            case KAKAO -> {
                KakaoTokenResponse tokenResponse = kakaoOAuthClient.getAccessToken(authorizationCode);
                yield tokenResponse.getAccessToken();
            }
            default -> throw new UnsupportedOperationException(provider + " OAuth는 아직 구현되지 않았습니다.");
        };
    }
    
    /**
     * Provider별 사용자 정보 조회
     */
    private OAuthUserInfo getUserInfoByProvider(OAuthProvider provider, String accessToken) {
        return switch (provider) {
            case KAKAO -> kakaoOAuthClient.getUserInfo(accessToken);
            default -> throw new UnsupportedOperationException(provider + " OAuth는 아직 구현되지 않았습니다.");
        };
    }
    
    /**
     * OAuth 로그인 결과 클래스
     */
    public static class OAuthLoginResult {
        private final boolean signupCompleted;
        private final TokenResponse tokenResponse;
        
        private OAuthLoginResult(boolean signupCompleted, TokenResponse tokenResponse) {
            this.signupCompleted = signupCompleted;
            this.tokenResponse = tokenResponse;
        }
        
        public static OAuthLoginResult loginSuccess(TokenResponse tokenResponse) {
            return new OAuthLoginResult(true, tokenResponse);
        }
        
        public static OAuthLoginResult needsAdditionalSignup(TokenResponse tokenResponse) {
            return new OAuthLoginResult(false, tokenResponse);
        }
        
        public boolean isSignupCompleted() {
            return signupCompleted;
        }
        
        public TokenResponse getTokenResponse() {
            return tokenResponse;
        }
    }
}
