package com.swyp10.entity;

import com.swyp10.domain.auth.entity.LoginType;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OAuthAccount Entity 테스트")
class OAuthAccountTest {

    @Test
    @DisplayName("OAuthAccount 엔티티 생성")
    void createOAuthAccount() {
        // given & when
        OAuthAccount oauthAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("123456789")
                .providerEmail("test@example.com")
                .providerNickname("테스트사용자")
                .providerProfileImage("https://example.com/profile.jpg")
                .build();

        // then
        assertThat(oauthAccount.getProvider()).isEqualTo(LoginType.KAKAO);
        assertThat(oauthAccount.getProviderUserId()).isEqualTo("123456789");
        assertThat(oauthAccount.getProviderEmail()).isEqualTo("test@example.com");
        assertThat(oauthAccount.getProviderNickname()).isEqualTo("테스트사용자");
        assertThat(oauthAccount.getProviderProfileImage()).isEqualTo("https://example.com/profile.jpg");
        assertThat(oauthAccount.getUser()).isNull();
    }

    @Test
    @DisplayName("OAuthAccount와 User 연결")
    void linkWithUser() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트사용자")
                .signupCompleted(true)
                .build();

        OAuthAccount oauthAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("123456789")
                .providerEmail("test@example.com")
                .providerNickname("테스트사용자")
                .providerProfileImage("https://example.com/profile.jpg")
                .build();

        // when
        oauthAccount.setUser(user);

        // then
        assertThat(oauthAccount.getUser()).isNotNull();
        assertThat(oauthAccount.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(oauthAccount.getUser().getNickname()).isEqualTo("테스트사용자");
    }

    @Test
    @DisplayName("OAuthAccount 정보 업데이트")
    void updateOAuthAccount() {
        // given
        OAuthAccount oauthAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("123456789")
                .providerEmail("old@example.com")
                .providerNickname("이전사용자")
                .providerProfileImage("https://example.com/old-profile.jpg")
                .build();

        // when
        oauthAccount.setProviderEmail("new@example.com");
        oauthAccount.setProviderNickname("새로운사용자");
        oauthAccount.setProviderProfileImage("https://example.com/new-profile.jpg");

        // then
        assertThat(oauthAccount.getProviderEmail()).isEqualTo("new@example.com");
        assertThat(oauthAccount.getProviderNickname()).isEqualTo("새로운사용자");
        assertThat(oauthAccount.getProviderProfileImage()).isEqualTo("https://example.com/new-profile.jpg");
    }

    @Test
    @DisplayName("OAuthAccount 기본값 검증")
    void checkDefaultValues() {
        // given & when
        OAuthAccount oauthAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("123456789")
                .build();

        // then
        assertThat(oauthAccount.getProvider()).isEqualTo(LoginType.KAKAO);
        assertThat(oauthAccount.getProviderUserId()).isEqualTo("123456789");
        assertThat(oauthAccount.getProviderEmail()).isNull();
        assertThat(oauthAccount.getProviderNickname()).isNull();
        assertThat(oauthAccount.getProviderProfileImage()).isNull();
        assertThat(oauthAccount.getUser()).isNull();
    }
}
