package com.swyp10.service.auth.common;

import com.swyp10.entity.OAuthAccount;
import com.swyp10.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class JwtTokenService {
    
    @Value("${jwt.secret:your-secret-key-here-must-be-at-least-256-bits-long}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400}")
    private Long expiration;
    
    /**
     * SecretKey 생성
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Access Token 생성 (일반 사용자용)
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        String token = Jwts.builder()
            .setSubject(String.valueOf(user.getUserId())) // 사용자 ID
            .claim("email", user.getEmail())
            .claim("nickname", user.getNickname())
            .claim("loginType", user.getLoginType().name())
            .claim("signupCompleted", user.getSignupCompleted())
            .claim("tokenType", "USER") // 토큰 타입 구분
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
        
        log.info("JWT Access Token 생성 (User): userId={}, nickname={}, expiry={}", 
                user.getUserId(), user.getNickname(), expiryDate);
        
        return token;
    }
    
    /**
     * OAuth 계정용 임시 토큰 생성 (회원가입 미완료)
     */
    public String generateOAuthToken(OAuthAccount oauthAccount) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        String token = Jwts.builder()
            .setSubject(String.valueOf(oauthAccount.getOauthId())) // OAuth 계정 ID
            .claim("provider", oauthAccount.getProvider().name())
            .claim("providerUserId", oauthAccount.getProviderUserId())
            .claim("providerNickname", oauthAccount.getProviderNickname())
            .claim("providerEmail", oauthAccount.getProviderEmail())
            .claim("signupCompleted", false)
            .claim("tokenType", "OAUTH") // 토큰 타입 구분
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
        
        log.info("JWT OAuth Token 생성: oauthAccountId={}, provider={}, expiry={}", 
                oauthAccount.getOauthId(), oauthAccount.getProvider(), expiryDate);
        
        return token;
    }
    
    /**
     * Token 검증
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
            return false;
        }
    }
    
    /**
     * Token에서 사용자 ID 추출 (USER 토큰만)
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String tokenType = claims.get("tokenType", String.class);
        
        if (!"USER".equals(tokenType)) {
            throw new RuntimeException("완전한 회원가입이 필요합니다.");
        }
        
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * Token에서 OAuth 계정 ID 추출 (OAUTH 토큰만)
     */
    public Long getOAuthAccountIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String tokenType = claims.get("tokenType", String.class);
        
        if (!"OAUTH".equals(tokenType)) {
            throw new RuntimeException("OAuth 토큰이 아닙니다.");
        }
        
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * Token 타입 확인
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("tokenType", String.class);
    }
    
    /**
     * 회원가입 완료 여부 확인
     */
    public boolean isSignupCompleted(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("signupCompleted", Boolean.class);
    }
    
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
        } catch (Exception e) {
            log.error("토큰에서 클레임 추출 실패: {}", e.getMessage());
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
    }
}
