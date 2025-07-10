package com.swyp10.domain.user.service.auth.common;

import com.swyp10.domain.user.constants.AuthConstants;
import com.swyp10.domain.user.constants.TokenType;
import com.swyp10.domain.user.entity.OAuthAccount;
import com.swyp10.domain.user.entity.User;
import com.swyp10.global.exception.ApplicationException;
import com.swyp10.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 통합 관리 서비스
 */
@Service
@Slf4j
public class TokenService {
    
    @Value("${jwt.secret:your-secret-key-here-must-be-at-least-256-bits-long}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400}")
    private Long expiration;
    
    // === 토큰 생성 ===
    
    /**
     * Access Token 생성 (일반 사용자용)
     */
    public String generateAccessToken(User user) {
        try {
            Date[] dates = calculateTokenDates();
            
            String token = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .claim(AuthConstants.EMAIL_CLAIM, user.getEmail())
                .claim(AuthConstants.NICKNAME_CLAIM, user.getNickname())
                .claim(AuthConstants.LOGIN_TYPE_CLAIM, user.getLoginType().name())
                .claim(AuthConstants.SIGNUP_COMPLETED_CLAIM, user.getSignupCompleted())
                .claim(AuthConstants.TOKEN_TYPE_CLAIM, TokenType.USER.getValue())
                .setIssuedAt(dates[0])
                .setExpiration(dates[1])
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
            
            log.info("JWT User Token 생성: userId={}, nickname={}", 
                    user.getUserId(), user.getNickname());
            
            return token;
            
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.TOKEN_GENERATION_FAILED, user.getUserId().toString(), e);
        }
    }
    
    /**
     * OAuth 계정용 임시 토큰 생성
     */
    public String generateOAuthToken(OAuthAccount oauthAccount) {
        try {
            Date[] dates = calculateTokenDates();
            
            String token = Jwts.builder()
                .setSubject(String.valueOf(oauthAccount.getOauthId()))
                .claim(AuthConstants.PROVIDER_CLAIM, oauthAccount.getProvider().name())
                .claim(AuthConstants.PROVIDER_USER_ID_CLAIM, oauthAccount.getProviderUserId())
                .claim(AuthConstants.PROVIDER_NICKNAME_CLAIM, oauthAccount.getProviderNickname())
                .claim(AuthConstants.PROVIDER_EMAIL_CLAIM, oauthAccount.getProviderEmail())
                .claim(AuthConstants.SIGNUP_COMPLETED_CLAIM, false)
                .claim(AuthConstants.TOKEN_TYPE_CLAIM, TokenType.OAUTH.getValue())
                .setIssuedAt(dates[0])
                .setExpiration(dates[1])
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
            
            log.info("JWT OAuth Token 생성: oauthAccountId={}, provider={}", 
                    oauthAccount.getOauthId(), oauthAccount.getProvider());
            
            return token;
            
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.TOKEN_GENERATION_FAILED, oauthAccount.getOauthId().toString(), e);
        }
    }
    
    // === 토큰 검증 ===
    
    /**
     * Token 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new ApplicationException(ErrorCode.INVALID_TOKEN, e.getMessage(), e);
        }
    }
    
    /**
     * Authorization 헤더에서 JWT 토큰 추출 및 검증
     */
    public String extractAndValidateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            throw new ApplicationException(ErrorCode.MISSING_REQUEST_HEADER, "인증 헤더가 올바르지 않습니다");
        }
        
        String token = authHeader.replace(AuthConstants.BEARER_PREFIX, "");
        validateToken(token); // 이미 예외를 던지므로 return
        return token;
    }
    
    // === 토큰 정보 추출 ===
    
    /**
     * Token에서 모든 클레임 추출
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (JwtException e) {
            throw new ApplicationException(ErrorCode.TOKEN_PARSING_FAILED, e.getMessage(), e);
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.TOKEN_PARSING_FAILED, "토큰 처리 중 오류 발생", e);
        }
    }
    
    /**
     * Token 타입 확인
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        String tokenTypeValue = claims.get(AuthConstants.TOKEN_TYPE_CLAIM, String.class);
        
        if (tokenTypeValue == null) {
            throw new ApplicationException(ErrorCode.UNKNOWN_TOKEN_TYPE);
        }
        
        return tokenTypeValue;
    }
    
    /**
     * USER 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String tokenType = getTokenType(token);
        
        if (!TokenType.USER.getValue().equals(tokenType)) {
            throw new ApplicationException(ErrorCode.ADDITIONAL_SIGNUP_REQUIRED);
        }
        
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * OAUTH 토큰에서 OAuth 계정 ID 추출
     */
    public Long getOAuthAccountIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String tokenType = getTokenType(token);
        
        if (!TokenType.OAUTH.getValue().equals(tokenType)) {
            throw new ApplicationException(ErrorCode.OAUTH_TOKEN_REQUIRED);
        }
        
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * 회원가입 완료 여부 확인
     */
    public boolean isSignupCompleted(String token) {
        Claims claims = getClaimsFromToken(token);
        Boolean signupCompleted = claims.get(AuthConstants.SIGNUP_COMPLETED_CLAIM, Boolean.class);
        return signupCompleted != null && signupCompleted;
    }
    
    // === Private 메서드들 ===
    
    /**
     * SecretKey 생성
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 현재 시간과 만료 시간 계산
     */
    private Date[] calculateTokenDates() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        return new Date[]{now, expiryDate};
    }
}
