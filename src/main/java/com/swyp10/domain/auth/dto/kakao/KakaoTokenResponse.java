package com.swyp10.domain.auth.dto.kakao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "카카오 토큰 응답")
public class KakaoTokenResponse {
    @Schema(description = "액세스 토큰", required = false, nullable = false, example = "ya29.a0AfH6SMC...")
    private String accessToken;
    
    @Schema(description = "리프레시 토큰", required = false, nullable = true, example = "1//0G...")
    private String refreshToken;
    
    @Schema(description = "토큰 만료 시간 (초)", required = false, nullable = false, example = "21599")
    private Long expiresIn;
    
    @Schema(description = "토큰 타입", required = false, nullable = false, example = "bearer")
    private String tokenType;
    
    @Schema(description = "스코프", required = false, nullable = true, example = "profile_nickname profile_image")
    private String scope;
}
