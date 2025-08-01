package com.swyp10.domain.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Schema(description = "카카오 사용자 정보 응답")
public class KakaoUserResponse {
    
    @Schema(description = "카카오 사용자 고유 ID", required = false, nullable = false, example = "1234567890")
    private Long id;
    
    @JsonProperty("kakao_account")
    @Schema(description = "카카오 계정 정보", required = false, nullable = false)
    private KakaoAccount kakaoAccount;
    
    @Getter
    @NoArgsConstructor
    @Schema(description = "카카오 계정 정보")
    public static class KakaoAccount {
        @Schema(description = "이메일 주소 (선택동의시 null 가능)", required = false, nullable = true, example = "user@example.com")
        private String email;
        
        @Schema(description = "프로필 정보", required = false, nullable = false)
        private Profile profile;
        
        @Getter
        @NoArgsConstructor
        @Schema(description = "카카오 프로필 정보")
        public static class Profile {
            @Schema(description = "닉네임", required = false, nullable = false, example = "홍길동")
            private String nickname;
            
            @JsonProperty("profile_image_url")
            @Schema(description = "프로필 이미지 URL", required = false, nullable = true, example = "https://example.com/profile.jpg")
            private String profileImageUrl;
            
            @JsonProperty("thumbnail_image_url")
            @Schema(description = "썸네일 이미지 URL", required = false, nullable = true, example = "https://example.com/thumbnail.jpg")
            private String thumbnailImageUrl;
        }
    }
}
