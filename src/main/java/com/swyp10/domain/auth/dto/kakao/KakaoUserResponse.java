package com.swyp10.domain.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class KakaoUserResponse {
    
    private Long id;
    
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;
    
    @Getter
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;
        private Profile profile;
        
        @Getter
        @NoArgsConstructor
        public static class Profile {
            private String nickname;
            
            @JsonProperty("profile_image_url")
            private String profileImageUrl;
            
            @JsonProperty("thumbnail_image_url")
            private String thumbnailImageUrl;
        }
    }
}
