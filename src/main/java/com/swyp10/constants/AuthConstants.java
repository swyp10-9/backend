package com.swyp10.constants;

/**
 * 인증 관련 상수 클래스
 */
public final class AuthConstants {
    
    // JWT 관련 상수
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    public static final String EMAIL_CLAIM = "email";
    public static final String NICKNAME_CLAIM = "nickname";
    public static final String LOGIN_TYPE_CLAIM = "loginType";
    public static final String SIGNUP_COMPLETED_CLAIM = "signupCompleted";
    public static final String PROVIDER_CLAIM = "provider";
    public static final String PROVIDER_USER_ID_CLAIM = "providerUserId";
    public static final String PROVIDER_NICKNAME_CLAIM = "providerNickname";
    public static final String PROVIDER_EMAIL_CLAIM = "providerEmail";
    
    // OAuth 관련 상수
    public static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    public static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    
    // HTTP 헤더 상수
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded;charset=utf-8";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    // 기본값 상수
    public static final long DEFAULT_TOKEN_EXPIRATION = 86400; // 24시간 (초)
    public static final String DEFAULT_PROFILE_IMAGE = "";
    
    private AuthConstants() {
        // 인스턴스 생성 방지
    }
}
