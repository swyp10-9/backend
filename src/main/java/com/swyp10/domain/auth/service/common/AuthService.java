package com.swyp10.domain.auth.service.common;

import com.swyp10.constants.TokenType;
import com.swyp10.domain.auth.dto.OAuthProvider;
import com.swyp10.domain.auth.dto.common.*;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;

import com.swyp10.domain.auth.service.kakao.KakaoOAuthClient;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
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
     * OAuth 인가 코드로 로그인 처리 - 바로 User 생성 (기본 origin 사용)
     */
    @Transactional
    public TokenResponse processOAuthLogin(String provider, String code) {
        return processOAuthLogin(provider, code, null);
    }
    
    /**
     * OAuth 인가 코드로 로그인 처리 - 바로 User 생성 (동적 origin 사용)
     */
    @Transactional
    public TokenResponse processOAuthLogin(String provider, String code, String origin) {
        OAuthProvider oauthProvider = OAuthProvider.fromString(provider);
        
        log.info("{} OAuth 로그인 요청: code={}, origin={}", provider, 
                code.substring(0, Math.min(code.length(), 10)) + "...", origin);
        
        // OAuth 클라이언트 선택 (현재는 카카오만 지원)
        OAuthClient oauthClient = oauthClientFactory.getClient(oauthProvider);
        
        // 1단계: 인가 코드로 액세스 토큰 발급
        String accessToken;
        if (origin != null && oauthClient instanceof KakaoOAuthClient) {
            // 카카오의 경우 동적 origin 사용
            accessToken = ((KakaoOAuthClient) oauthClient)
                    .getAccessToken(code, origin);
        } else {
            // 기본 방식 사용
            accessToken = oauthClient.getAccessToken(code);
        }
        
        // 2단계: 액세스 토큰으로 사용자 정보 조회
        OAuthUserInfo oauthUserInfo = oauthClient.getUserInfo(accessToken);
        
        // 3단계: 기존 사용자 확인 또는 새 사용자 생성
        User user = userService.findByKakaoIdOrCreate(oauthUserInfo);
        
        // 4단계: 사용자 토큰 생성 및 반환
        String userAccessToken = tokenService.generateAccessToken(user);
        
        log.info("카카오 OAuth 로그인 성공: userId={}, kakaoId={}", 
                user.getUserId(), user.getEmail());
        
        return TokenResponse.of(userAccessToken, user.getUserId(), user.getNickname());
    }
    
    /**
     * 추가 회원가입 완료 처리 (기존 계정 연동 지원)
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
        
        // 기존 계정이 있는지 확인
        boolean existingUserExists = userService.existsByEmail(request.getEmail());
        
        User user;
        if (existingUserExists) {
            // 기존 계정이 있는 경우 - 비밀번호 검증 후 연동
            User existingUser = userService.findByEmail(request.getEmail());
            
            // 비밀번호 검증
            if (!userService.validatePassword(request.getPassword(), existingUser.getPassword())) {
                log.warn("기존 계정 연동 실패 - 비밀번호 불일치: email={}", request.getEmail());
                throw new ApplicationException(ErrorCode.INVALID_PASSWORD);
            }
            
            // OAuth 계정과 기존 사용자 연동
            accountService.linkOAuthAccountToUser(oauthAccountId, existingUser.getUserId());
            user = existingUser;
            
            log.info("OAuth 계정 연동 완료: oauthAccountId={}, userId={}, email={}", 
                    oauthAccountId, existingUser.getUserId(), request.getEmail());
        } else {
            // 기존 계정이 없는 경우 - 새로운 회원가입
            user = userService.completeOAuthSignup(oauthAccountId, request);
            log.info("새 사용자 OAuth 회원가입 완료: userId={}, email={}", user.getUserId(), request.getEmail());
        }
        
        // 완전한 USER 토큰 생성
        String accessToken = tokenService.generateAccessToken(user);
        TokenResponse tokenResponse = TokenResponse.of(accessToken, user.getUserId(), user.getNickname());
        
        log.info("OAuth 추가 처리 완료: userId={}, email={}", user.getUserId(), request.getEmail());
        
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
     * 이메일 사용자가 OAuth 계정 연동
     */
    @Transactional
    public void linkOAuthToEmailUser(String provider, String code, String authHeader) {
        // 사용자 토큰 검증 및 추출
        String token = tokenService.extractAndValidateToken(authHeader);
        if (token == null) {
            throw new ApplicationException(ErrorCode.INVALID_TOKEN);
        }
        
        // 토큰이 USER 타입인지 확인
        String tokenType = tokenService.getTokenType(token);
        if (!TokenType.USER.getValue().equals(tokenType)) {
            throw new ApplicationException(ErrorCode.USER_TOKEN_REQUIRED);
        }
        
        // 토큰에서 사용자 ID 추출
        Long userId = tokenService.getUserIdFromToken(token);
        User user = userService.findById(userId);
        
        log.info("OAuth 연동 요청: userId={}, provider={}", userId, provider);
        
        OAuthProvider oauthProvider = OAuthProvider.fromString(provider);
        
        // OAuth 클라이언트 선택
        OAuthClient oauthClient = oauthClientFactory.getClient(oauthProvider);
        
        // 1단계: 인가 코드로 액세스 토큰 발급
        String accessToken = oauthClient.getAccessToken(code);
        
        // 2단계: 액세스 토큰으로 사용자 정보 조회
        OAuthUserInfo oauthUserInfo = oauthClient.getUserInfo(accessToken);
        
        // 3단계: 이미 연동된 OAuth 계정이 있는지 확인
        boolean isAlreadyLinked = accountService.isOAuthAccountLinked(oauthUserInfo);
        if (isAlreadyLinked) {
            throw new ApplicationException(ErrorCode.OAUTH_ACCOUNT_ALREADY_LINKED);
        }
        
        // 4단계: OAuth 계정 찾기 또는 생성
        OAuthAccount oauthAccount = accountService.findOrCreate(oauthUserInfo);
        
        // 5단계: OAuth 계정과 사용자 연동
        if (oauthAccount.getUser() != null) {
            // 이미 다른 사용자와 연동된 경우
            throw new ApplicationException(ErrorCode.OAUTH_ACCOUNT_ALREADY_LINKED);
        }
        
        accountService.linkOAuthAccountToUser(oauthAccount.getOauthId(), userId);
        
        log.info("OAuth 연동 완료: userId={}, provider={}, oauthAccountId={}", 
                userId, provider, oauthAccount.getOauthId());
    }
    
    /**
     * 토큰 연장
     */
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String authHeader) {
        // 토큰 추출 및 검증
        String token = tokenService.extractAndValidateToken(authHeader);
        if (token == null) {
            throw new ApplicationException(ErrorCode.INVALID_TOKEN);
        }
        
        // 토큰 연장
        String newToken = tokenService.refreshAccessToken(token);
        
        // 토큰 타입에 따라 응답 생성
        String tokenType = tokenService.getTokenType(newToken);
        
        if (TokenType.USER.getValue().equals(tokenType)) {
            // USER 토큰인 경우
            Long userId = tokenService.getUserIdFromToken(newToken);
            User user = userService.findById(userId);
            
            log.info("USER 토큰 연장 완료: userId={}", userId);
            return TokenResponse.of(newToken, user.getUserId(), user.getNickname());
            
        } else if (TokenType.OAUTH.getValue().equals(tokenType)) {
            // OAUTH 토큰인 경우
            Long oauthAccountId = tokenService.getOAuthAccountIdFromToken(newToken);
            OAuthAccount oauthAccount = accountService.findById(oauthAccountId);
            
            log.info("OAUTH 토큰 연장 완료: oauthAccountId={}", oauthAccountId);
            return TokenResponse.ofOAuth(newToken, oauthAccount.getProviderNickname());
            
        } else {
            throw new ApplicationException(ErrorCode.UNKNOWN_TOKEN_TYPE);
        }
    }
}
