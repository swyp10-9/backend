package com.swyp10.service.auth.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰 검증 및 추출 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthTokenService {
    
    private final JwtTokenService jwtTokenService;
    
    /**
     * Authorization 헤더에서 JWT 토큰 추출 및 검증
     */
    public String extractAndValidateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenService.validateToken(token) ? token : null;
    }
    
    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        return jwtTokenService.getUserIdFromToken(token);
    }
}
