package com.swyp10.domain.auth.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT 토큰 응답")
public class TokenResponse {
    
    @Schema(description = "JWT 액세스 토큰", required = true, nullable = false, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "토큰 타입", required = true, nullable = false, example = "Bearer")
    private String tokenType;
    
    @Schema(description = "토큰 만료 시간 (초)", required = true, nullable = false, example = "86400")
    private Long expiresIn;
    
    @Schema(description = "사용자 ID (OAuth 단계에서는 null)", required = false, nullable = true, example = "12345")
    private Long userId;
    
    @Schema(description = "사용자 닉네임", required = true, nullable = false, example = "홍길동")
    private String nickname;
    
    @Schema(description = "추가 회원가입 필요 여부", required = true, nullable = false, example = "false")
    private Boolean needsAdditionalSignup;
    
    /**
     * 완전한 회원가입 완료 사용자용 토큰 응답
     */
    public static TokenResponse of(String accessToken, Long userId, String nickname) {
        return TokenResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(86400L) // 24시간
            .userId(userId)
            .nickname(nickname)
            .needsAdditionalSignup(false)
            .build();
    }
    
    /**
     * OAuth 계정만 있는 사용자용 토큰 응답 (추가 회원가입 필요)
     */
    public static TokenResponse ofOAuth(String accessToken, String nickname) {
        return TokenResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(86400L) // 24시간
            .userId(null) // OAuth 단계에서는 userId 없음
            .nickname(nickname)
            .needsAdditionalSignup(true)
            .build();
    }
}
