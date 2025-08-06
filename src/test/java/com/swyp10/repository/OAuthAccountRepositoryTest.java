package com.swyp10.repository;

import com.swyp10.config.QueryDslConfig;
import com.swyp10.domain.auth.repository.OAuthAccountRepository;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.auth.entity.LoginType;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("OAuthAccountRepository 테스트")
class OAuthAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private UserRepository userRepository;

    private OAuthAccount testOAuthAccount;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트사용자")
                .signupCompleted(true)
                .build();

        testOAuthAccount = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("123456789")
                .providerEmail("test@example.com")
                .providerNickname("테스트사용자")
                .providerProfileImage("https://example.com/profile.jpg")
                .build();
    }

    @Test
    @DisplayName("OAuth 계정 저장 및 조회")
    void save_And_FindById() {
        // given
        OAuthAccount savedAccount = oauthAccountRepository.save(testOAuthAccount);
        entityManager.flush();

        // when
        Optional<OAuthAccount> found = oauthAccountRepository.findById(savedAccount.getOauthId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getProvider()).isEqualTo(LoginType.KAKAO);
        assertThat(found.get().getProviderUserId()).isEqualTo("123456789");
        assertThat(found.get().getProviderEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("제공자와 제공자 사용자 ID로 OAuth 계정 조회 - 존재하는 경우")
    void findByProviderAndProviderUserId_Exists() {
        // given
        oauthAccountRepository.save(testOAuthAccount);
        entityManager.flush();

        // when
        Optional<OAuthAccount> found = oauthAccountRepository.findByProviderAndProviderUserId(
                LoginType.KAKAO, "123456789");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getProvider()).isEqualTo(LoginType.KAKAO);
        assertThat(found.get().getProviderUserId()).isEqualTo("123456789");
        assertThat(found.get().getProviderEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("제공자와 제공자 사용자 ID로 OAuth 계정 조회 - 존재하지 않는 경우")
    void findByProviderAndProviderUserId_NotExists() {
        // when
        Optional<OAuthAccount> found = oauthAccountRepository.findByProviderAndProviderUserId(
                LoginType.KAKAO, "nonexistent");

        // then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("OAuth 계정과 사용자 연결")
    void linkOAuthAccountWithUser() {
        // given
        User savedUser = userRepository.save(testUser);
        testOAuthAccount.setUser(savedUser);
        OAuthAccount savedAccount = oauthAccountRepository.save(testOAuthAccount);
        entityManager.flush();

        // when
        Optional<OAuthAccount> found = oauthAccountRepository.findById(savedAccount.getOauthId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isNotNull();
        assertThat(found.get().getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(found.get().getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("다른 제공자의 OAuth 계정 조회")
    void findByDifferentProvider() {
        // given
        oauthAccountRepository.save(testOAuthAccount);
        entityManager.flush();

        // when
        Optional<OAuthAccount> found = oauthAccountRepository.findByProviderAndProviderUserId(
                LoginType.EMAIL, "123456789");

        // then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("동일한 제공자의 다른 사용자 ID로 조회")
    void findByDifferentProviderUserId() {
        // given
        oauthAccountRepository.save(testOAuthAccount);
        entityManager.flush();

        // when
        Optional<OAuthAccount> found = oauthAccountRepository.findByProviderAndProviderUserId(
                LoginType.KAKAO, "different-user-id");

        // then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("OAuth 계정 삭제")
    void deleteOAuthAccount() {
        // given
        OAuthAccount savedAccount = oauthAccountRepository.save(testOAuthAccount);
        entityManager.flush();

        // when
        oauthAccountRepository.delete(savedAccount);
        entityManager.flush();

        // then
        assertThat(oauthAccountRepository.findById(savedAccount.getOauthId())).isNotPresent();
    }

    @Test
    @DisplayName("여러 OAuth 계정 저장 및 조회")
    void saveMultipleOAuthAccounts() {
        // given
        OAuthAccount account1 = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("111111111")
                .providerEmail("user1@example.com")
                .providerNickname("사용자1")
                .build();

        OAuthAccount account2 = OAuthAccount.builder()
                .provider(LoginType.KAKAO)
                .providerUserId("222222222")
                .providerEmail("user2@example.com")
                .providerNickname("사용자2")
                .build();

        // when
        oauthAccountRepository.save(account1);
        oauthAccountRepository.save(account2);
        entityManager.flush();

        // then
        assertThat(oauthAccountRepository.findByProviderAndProviderUserId(
                LoginType.KAKAO, "111111111")).isPresent();
        assertThat(oauthAccountRepository.findByProviderAndProviderUserId(
                LoginType.KAKAO, "222222222")).isPresent();
    }
}
