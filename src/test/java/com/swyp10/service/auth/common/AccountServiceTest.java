package com.swyp10.service.auth.common;

import com.swyp10.domain.auth.service.common.AccountService;
import com.swyp10.domain.auth.dto.OAuthProvider;
import com.swyp10.domain.auth.dto.common.OAuthUserInfo;
import com.swyp10.domain.auth.entity.LoginType;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.domain.auth.repository.OAuthAccountRepository;
import com.swyp10.domain.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 테스트")
class AccountServiceTest {

    @Mock
    private OAuthAccountRepository oauthAccountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private OAuthUserInfo oauthUserInfo;
    private OAuthAccount oauthAccount;
    private User user;

    @BeforeEach
    void setUp() {
        oauthUserInfo = OAuthUserInfo.builder()
                .provider(OAuthProvider.KAKAO)
                .oauthId("123456789")
                .email("test@example.com")
                .nickname("테스트사용자")
                .profileImage("https://example.com/profile.jpg")
                .build();

        oauthAccount = OAuthAccount.builder()
                .oauthId(1L)
                .provider(LoginType.KAKAO)
                .providerUserId("123456789")
                .providerEmail("test@example.com")
                .providerNickname("테스트사용자")
                .providerProfileImage("https://example.com/profile.jpg")
                .build();

        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트사용자")
                .signupCompleted(true)
                .build();
    }

    @Test
    @DisplayName("OAuth 계정 생성 - 새로운 계정인 경우")
    void findOrCreate_NewAccount_Success() {
        // given
        when(oauthAccountRepository.findByProviderAndProviderUserId(LoginType.KAKAO, "123456789"))
                .thenReturn(Optional.empty());
        when(oauthAccountRepository.save(any(OAuthAccount.class)))
                .thenReturn(oauthAccount);

        // when
        OAuthAccount result = accountService.findOrCreate(oauthUserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProvider()).isEqualTo(LoginType.KAKAO);
        assertThat(result.getProviderUserId()).isEqualTo("123456789");
        verify(oauthAccountRepository).save(any(OAuthAccount.class));
    }

    @Test
    @DisplayName("OAuth 계정 조회 - 기존 계정인 경우")
    void findOrCreate_ExistingAccount_Success() {
        // given
        when(oauthAccountRepository.findByProviderAndProviderUserId(LoginType.KAKAO, "123456789"))
                .thenReturn(Optional.of(oauthAccount));

        // when
        OAuthAccount result = accountService.findOrCreate(oauthUserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOauthId()).isEqualTo(1L);
        verify(oauthAccountRepository, never()).save(any(OAuthAccount.class));
    }

    @Test
    @DisplayName("OAuth 계정 조회 - ID로 조회 성공")
    void findById_Success() {
        // given
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));

        // when
        OAuthAccount result = accountService.findById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOauthId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("OAuth 계정 조회 - 존재하지 않는 계정")
    void findById_NotFound_ThrowsException() {
        // given
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.findById(1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.OAUTH_ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("사용자와 OAuth 계정 연결 - 성공")
    void linkWithUser_Success() {
        // given
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));
        when(oauthAccountRepository.save(any(OAuthAccount.class)))
                .thenReturn(oauthAccount);

        // when
        accountService.linkWithUser(1L, user);

        // then
        verify(oauthAccountRepository).save(any(OAuthAccount.class));
    }

    @Test
    @DisplayName("OAuth 계정과 기존 사용자 연동 - 성공")
    void linkOAuthAccountToUser_Success() {
        // given
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(oauthAccountRepository.save(any(OAuthAccount.class)))
                .thenReturn(oauthAccount);

        // when
        accountService.linkOAuthAccountToUser(1L, 1L);

        // then
        verify(userRepository).findById(1L);
        verify(oauthAccountRepository).save(any(OAuthAccount.class));
    }

    @Test
    @DisplayName("OAuth 계정과 기존 사용자 연동 - 사용자 없음")
    void linkOAuthAccountToUser_UserNotFound_ThrowsException() {
        // given
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.linkOAuthAccountToUser(1L, 1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("OAuth 계정과 기존 사용자 연동 - 이미 연동된 계정")
    void linkOAuthAccountToUser_AlreadyLinked_ThrowsException() {
        // given
        oauthAccount.setUser(user);
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));

        // when & then
        assertThatThrownBy(() -> accountService.linkOAuthAccountToUser(1L, 1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.SIGNUP_ALREADY_COMPLETED.getMessage());
    }

    @Test
    @DisplayName("사용자와 OAuth 계정 연결 - 이미 연결된 계정")
    void linkWithUser_AlreadyLinked_ThrowsException() {
        // given
        oauthAccount.setUser(user);
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));

        // when & then
        assertThatThrownBy(() -> accountService.linkWithUser(1L, user))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.SIGNUP_ALREADY_COMPLETED.getMessage());
    }

    @Test
    @DisplayName("회원가입 완료 여부 확인 - 완료된 경우")
    void isSignupCompleted_True() {
        // given
        oauthAccount.setUser(user);
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));

        // when
        boolean result = accountService.isSignupCompleted(1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("회원가입 완료 여부 확인 - 미완료된 경우")
    void isSignupCompleted_False() {
        // given
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));

        // when
        boolean result = accountService.isSignupCompleted(1L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("OAuth 계정으로 사용자 찾기 - 성공")
    void findUserByOAuthAccount_Success() {
        // given
        oauthAccount.setUser(user);
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));

        // when
        User result = accountService.findUserByOAuthAccount(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("OAuth 계정으로 사용자 찾기 - 연결된 사용자 없음")
    void findUserByOAuthAccount_NoLinkedUser_ThrowsException() {
        // given
        when(oauthAccountRepository.findById(1L))
                .thenReturn(Optional.of(oauthAccount));

        // when & then
        assertThatThrownBy(() -> accountService.findUserByOAuthAccount(1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.ADDITIONAL_SIGNUP_REQUIRED.getMessage());
    }
}
