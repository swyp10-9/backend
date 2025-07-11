package com.swyp10.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Entity 테스트")
class UserTest {

    @Test
    @DisplayName("User 엔티티 생성")
    void createUser() {
        // given & when
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트사용자")
                .signupCompleted(true)
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getNickname()).isEqualTo("테스트사용자");
        assertThat(user.getSignupCompleted()).isTrue();
    }

    @Test
    @DisplayName("User 엔티티 생성 - 프로필 이미지 포함")
    void createUserWithProfile() {
        // given & when
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트사용자")
                .profileImage("https://example.com/profile.jpg")
                .signupCompleted(true)
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getNickname()).isEqualTo("테스트사용자");
        assertThat(user.getProfileImage()).isEqualTo("https://example.com/profile.jpg");
        assertThat(user.getSignupCompleted()).isTrue();
    }

    @Test
    @DisplayName("User 엔티티 기본값 검증")
    void checkDefaultValues() {
        // given & when
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트사용자")
                .build();

        // then
        assertThat(user.getSignupCompleted()).isFalse();
        assertThat(user.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("User 엔티티 수정")
    void updateUser() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트사용자")
                .signupCompleted(false)
                .build();

        // when
        user.setNickname("수정된사용자");
        user.setSignupCompleted(true);
        user.setProfileImage("https://example.com/new-profile.jpg");

        // then
        assertThat(user.getNickname()).isEqualTo("수정된사용자");
        assertThat(user.getSignupCompleted()).isTrue();
        assertThat(user.getProfileImage()).isEqualTo("https://example.com/new-profile.jpg");
    }
}
