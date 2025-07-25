package com.swyp10.domain.auth.dto.kakao;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoTokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private String scope;
}
