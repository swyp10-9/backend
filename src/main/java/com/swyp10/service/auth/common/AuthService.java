package com.swyp10.service.auth.common;

import com.swyp10.constants.TokenType;
import com.swyp10.dto.auth.OAuthProvider;
import com.swyp10.dto.auth.common.LoginRequest;
import com.swyp10.dto.auth.common.SignupRequest;
import com.swyp10.dto.auth.common.TokenResponse;

import com.swyp10.dto.auth.common.OAuthUserInfo;
import com.swyp10.entity.OAuthAccount;
import com.swyp10.entity.User;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 전체 플로우를 관리하는 통합 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final OAuthClientFactory oauthClientFactory;
    private final EmailService emailService;
    private final UserService userService;
    private final AccountService accountService;
    private final TokenService tokenService;
    
    /**
     * OAuth 인가 코드로 로그인 처리
     */
    @Transactional
    public TokenResponse processOAuthLogin(String provider, String code) {
        OAuthProvider oauthProvider = OAuthProvider.fromString(provider);
        
        log.info("{} OAuth 로그인 요청: code={}", provider, code.substring(0, Math.min(code.length(), 10)) + "...");
        
        // OAuth 클라이언트 선택
        OAuthClient oauthClient = oauthClientFactory.getClient(oauthProvider);
        
        // 1단계: 인가 코드로 액세스 토큰 발급
        String accessToken = oauthClient.getAccessToken(code);
        
        // 2단계: 액세스 토큰으로 사용자 정보 조회
        OAuthUserInfo oauthUserInfo = oauthClient.getUserInfo(accessToken);
        
        // 3단계: OAuth 계정 찾기 또는 생성
        OAuthAccount oauthAccount = accountService.findOrCreate(oauthUserInfo);
        
        // 4단계: 회원가입 완료 여부 확인
        boolean isSignupCompleted = accountService.isSignupCompleted(oauthAccount.getOauthId());
        
        if (isSignupCompleted) {
            // 회원가입이 완료된 경우: USER 토큰 생성
            User user = accountService.findUserByOAuthAccount(oauthAccount.getOauthId());
            String completedAccessToken = tokenService.generateAccessToken(user);
            
            log.info("OAuth 로그인 성공 (회원가입 완료): provider={}, userId={}", 
                    provider, user.getUserId());
            
            return TokenResponse.of(completedAccessToken, user.getUserId(), user.getNickname());
        } else {
            // 회원가입이 미완료된 경우: OAUTH 토큰 생성
            String oauthToken = tokenService.generateOAuthToken(oauthAccount);
            
            log.info("OAuth 로그인 성공 (추가 회원가입 필요): provider={}, oauthAccountId={}", 
                    provider, oauthAccount.getOauthId());
            
            return TokenResponse.ofOAuth(oauthToken, oauthAccount.getProviderNickname());
        }
    }
    
    /**
     * 추가 회원가입 완료 처리
     */
    @Transactional
    public TokenResponse completeAdditionalSignup(String authHeader, SignupRequest request) {
        // OAuth 토큰 검증 및 추출
        String token = tokenService.extractAndValidateToken(authHeader);
        if (token == null) {
            throw new ApplicationException(ErrorCode.INVALID_TOKEN);
        }
        
        // 토큰이 OAuth 타입인지 확인
        String tokenType = tokenService.getTokenType(token);
        if (!TokenType.OAUTH.getValue().equals(tokenType)) {
            throw new ApplicationException(ErrorCode.OAUTH_TOKEN_REQUIRED);
        }
        
        // 토큰에서 OAuth 계정 ID 추출
        Long oauthAccountId = tokenService.getOAuthAccountIdFromToken(token);
        
        log.info("추가 회원가입 요청: oauthAccountId={}, email={}", oauthAccountId, request.getEmail());
        
        // 추가 회원가입 완료
        User user = userService.completeOAuthSignup(oauthAccountId, request);
        
        // 완전한 USER 토큰 생성
        String accessToken = tokenService.generateAccessToken(user);
        TokenResponse tokenResponse = TokenResponse.of(accessToken, user.getUserId(), user.getNickname());
        
        log.info("추가 회원가입 완료: userId={}, email={}", user.getUserId(), request.getEmail());
        
        return tokenResponse;
    }
    
    /**
     * 현재 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserInfo getCurrentUser(String authHeader) {
        String token = tokenService.extractAndValidateToken(authHeader);
        if (token == null) {
            throw new ApplicationException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰 타입 확인
        String tokenType = tokenService.getTokenType(token);

        if (TokenType.OAUTH.getValue().equals(tokenType)) {
            // OAuth 토큰인 경우 - 추가 회원가입 필요
            throw new ApplicationException(ErrorCode.ADDITIONAL_SIGNUP_REQUIRED);
        } else if (TokenType.USER.getValue().equals(tokenType)) {
            // 완전한 사용자 토큰인 경우
            Long userId = tokenService.getUserIdFromToken(token);
            User user = userService.findById(userId);

            log.info("사용자 정보 조회: userId={}", userId);

            return UserInfo.from(user);
        } else {
            throw new ApplicationException(ErrorCode.UNKNOWN_TOKEN_TYPE);
        }
    }
    
    /**
     * 이메일 회원가입
     */
    @Transactional
    public TokenResponse signup(SignupRequest request) {
        return emailService.signup(request);
    }
    
    /**
     * 이메일 로그인
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        return emailService.login(request);
    }
    
    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean checkEmailAvailable(String email) {
        return emailService.isEmailAvailable(email);
    }
    
    /**
     * 사용자 정보 응답 DTO
     */
    public static class UserInfo {
        private final Long userId;
        private final String email;
        private final String nickname;
        
        private UserInfo(Long userId, String email, String nickname) {
            this.userId = userId;
            this.email = email;
            this.nickname = nickname;
        }
        
        public static UserInfo from(User user) {
            return new UserInfo(user.getUserId(), user.getEmail(), user.getNickname());
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getNickname() {
            return nickname;
        }
    }
}
