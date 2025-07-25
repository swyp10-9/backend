package com.swyp10.domain.auth.dto.common;

import com.swyp10.domain.auth.dto.OAuthProvider;
import com.swyp10.domain.auth.dto.kakao.KakaoUserResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfo {
    
    private String email;
    private String nickname;
    private String profileImage;
    private String oauthId;  // OAuth 고유 ID
    private OAuthProvider provider;

    // Kakao 응답을 OAuthUserInfo로 변환하는 팩토리 메서드
    public static OAuthUserInfo fromKakao(KakaoUserResponse kakaoResponse) {
        if (kakaoResponse == null) {
            throw new IllegalArgumentException("카카오 응답이 null입니다.");
        }

        if (kakaoResponse.getKakaoAccount() == null) {
            throw new IllegalArgumentException("카카오 계정 정보가 없습니다.");
        }

        if (kakaoResponse.getKakaoAccount().getProfile() == null) {
            throw new IllegalArgumentException("카카오 프로필 정보가 없습니다.");
        }

        String email = kakaoResponse.getKakaoAccount().getEmail();
        String nickname = kakaoResponse.getKakaoAccount().getProfile().getNickname();

        // 닉네임이 없으면 기본값 설정
        if (nickname == null || nickname.isEmpty()) {
            nickname = "카카오사용자" + kakaoResponse.getId();
        }

        return OAuthUserInfo.builder()
            .oauthId(String.valueOf(kakaoResponse.getId()))
            .email(email) // null 가능 (선택동의)
            .nickname(nickname)
            .profileImage(kakaoResponse.getKakaoAccount().getProfile().getProfileImageUrl())
            .provider(OAuthProvider.KAKAO)
            .build();
    }
    
    // 미래 확장을 위한 메서드들
    // public static OAuthUserInfo fromGoogle(GoogleUserResponse googleResponse) { ... }
    // public static OAuthUserInfo fromNaver(NaverUserResponse naverResponse) { ... }
}
