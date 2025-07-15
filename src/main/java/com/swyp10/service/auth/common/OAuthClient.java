package com.swyp10.service.auth.common;

import com.swyp10.dto.auth.common.OAuthUserInfo;

/**
 * OAuth 클라이언트 공통 인터페이스
 * 모든 OAuth 제공자(카카오, 구글, 네이버 등)가 구현해야 하는 기본 메서드들을 정의
 */
public interface OAuthClient {
    
    /**
     * 인가 코드로 액세스 토큰 발급
     * @param code OAuth 인가 코드
     * @return 액세스 토큰
     */
    String getAccessToken(String code);
    
    /**
     * 액세스 토큰으로 사용자 정보 조회
     * @param accessToken OAuth 액세스 토큰
     * @return 표준화된 사용자 정보
     */
    OAuthUserInfo getUserInfo(String accessToken);
}
