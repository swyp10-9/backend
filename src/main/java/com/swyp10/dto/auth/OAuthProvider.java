package com.swyp10.dto.auth;

public enum OAuthProvider {
    KAKAO("kakao");
    // 필요시 다른 제공자 추가: GOOGLE("google"), NAVER("naver"), APPLE("apple")
    
    private final String value;
    
    OAuthProvider(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static OAuthProvider fromString(String provider) {
        for (OAuthProvider p : OAuthProvider.values()) {
            if (p.value.equalsIgnoreCase(provider)) {
                return p;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
    }
}
