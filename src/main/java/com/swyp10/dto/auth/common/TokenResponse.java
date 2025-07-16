package com.swyp10.dto.auth.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String nickname;
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
