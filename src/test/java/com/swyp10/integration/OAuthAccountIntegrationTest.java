package com.swyp10.integration;

import com.swyp10.domain.auth.entity.LoginType;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.OAuthAccountRepository;
import com.swyp10.domain.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@IntegrationTest
@DisplayName("OAuth 계정 통합 테스트")
class OAuthAccountIntegrationTest {

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("OAuth 계정 저장 및 조회 - 전체 플로우")
    void saveAndFindOAuthAccount_FullFlow() {
        // given
        OAuthAccount oauthAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("integration123456")
                .providerEmail("integration@kakao.com")
                .providerNickname("통합테스트카카오")
                .providerProfileImage("https://example.com/integration.jpg")
                .build();

        // when
        OAuthAccount savedAccount = oauthAccountRepository.save(oauthAccount);

        // then
        assertThat(savedAccount.getOauthId()).isNotNull();
        assertThat(savedAccount.getCreatedAt()).isNotNull();
        assertThat(savedAccount.getUpdatedAt()).isNotNull();

        // 조회 검증
        Optional<OAuthAccount> foundAccount = oauthAccountRepository.findById(savedAccount.getOauthId());
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getProviderUserId()).isEqualTo("integration123456");
        assertThat(foundAccount.get().getProvider()).isEqualTo(LoginType.KAKAO);
    }

    @Test
    @DisplayName("OAuth 계정과 사용자 연결 - 전체 플로우")
    void linkOAuthAccountWithUser_FullFlow() {
        // given
        // 1. 사용자 생성
        User user = User.builder()
                .email("oauth@integration.com")
                .password("test1234")
                .nickname("OAuth통합사용자")
                .signupCompleted(true)
                .build();
        User savedUser = userRepository.save(user);

        // 2. OAuth 계정 생성
        OAuthAccount oauthAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("linked123456")
                .providerEmail("oauth@integration.com")
                .providerNickname("OAuth통합사용자")
                .providerProfileImage("https://example.com/oauth.jpg")
                .build();

        // when
        // 3. 계정 연결
        oauthAccount.setUser(savedUser);
        OAuthAccount savedAccount = oauthAccountRepository.save(oauthAccount);

        // then
        assertThat(savedAccount.getUser()).isNotNull();
        assertThat(savedAccount.getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(savedAccount.getUser().getEmail()).isEqualTo("oauth@integration.com");

        // 양방향 연결 확인
        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());
        assertThat(foundUser).isPresent();
        // User 엔티티에서 OAuth 계정 리스트가 있다면 확인
        // assertThat(foundUser.get().getOAuthAccounts()).hasSize(1);
    }

    @Test
    @DisplayName("제공자별 OAuth 계정 조회")
    void findByProviderAndProviderUserId_Success() {
        // given
        OAuthAccount kakaoAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("kakao123456")
                .providerEmail("kakao@test.com")
                .providerNickname("카카오사용자")
                .build();

        oauthAccountRepository.save(kakaoAccount);

        // when
        Optional<OAuthAccount> foundAccount = oauthAccountRepository
                .findByProviderAndProviderUserId(LoginType.KAKAO, "kakao123456");

        // then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getProvider()).isEqualTo(LoginType.KAKAO);
        assertThat(foundAccount.get().getProviderUserId()).isEqualTo("kakao123456");
        assertThat(foundAccount.get().getProviderEmail()).isEqualTo("kakao@test.com");
    }
    @Test
    @DisplayName("OAuth 계정 정보 업데이트")
    void updateOAuthAccount_Success() throws InterruptedException {
        // given
        OAuthAccount oauthAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("update123456")
                .providerEmail("old@kakao.com")
                .providerNickname("이전닉네임")
                .providerProfileImage("https://example.com/old.jpg")
                .build();

        OAuthAccount savedAccount = oauthAccountRepository.save(oauthAccount);
        oauthAccountRepository.flush(); // 강제 플러시

        LocalDateTime createdTime = savedAccount.getCreatedAt(); // 생성 시간 저장

        // when
        Thread.sleep(10); // 10ms 대기

        savedAccount.setProviderEmail("new@kakao.com");
        savedAccount.setProviderNickname("새로운닉네임");
        savedAccount.setProviderProfileImage("https://example.com/new.jpg");

        OAuthAccount updatedAccount = oauthAccountRepository.save(savedAccount);
        oauthAccountRepository.flush(); // 강제 플러시

        //then
        assertThat(updatedAccount.getUpdatedAt()).isAfter(createdTime);
    }
    @Test
    @DisplayName("존재하지 않는 OAuth 계정 조회")
    void findNonExistentOAuthAccount() {
        // when
        Optional<OAuthAccount> foundAccount = oauthAccountRepository
                .findByProviderAndProviderUserId(LoginType.KAKAO, "nonexistent123456");

        // then
        assertThat(foundAccount).isNotPresent();
    }
}
