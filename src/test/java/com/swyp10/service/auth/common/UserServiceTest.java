package com.swyp10.service.auth.common;

import com.swyp10.dto.auth.common.SignupRequest;
import com.swyp10.entity.LoginType;
import com.swyp10.entity.OAuthAccount;
import com.swyp10.entity.User;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * UserService 테스트
 * 
 * 리팩토링된 사용자 관리 서비스의 핵심 기능들을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 통합 테스트")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountService accountService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User testUser;
    private OAuthAccount testOAuthAccount;
    private SignupRequest testSignupRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId(1L)
            .email("test@example.com")
            .nickname("테스트유저")
            .password("encoded_password")
            .signupCompleted(true)
            .build();

        testOAuthAccount = OAuthAccount.builder()
            .oauthId(1L)
            .provider(LoginType.KAKAO)
            .providerUserId("12345")
            .providerEmail("oauth@example.com")
            .providerNickname("OAuth유저")
            .providerProfileImage("profile.jpg")
            .build();

        testSignupRequest = SignupRequest.builder()
            .email("test@example.com")
            .password("password123!")
            .nickname("테스트유저")
            .build();
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class UserRetrievalTest {

        @Test
        @DisplayName("ID로 사용자 조회 성공")
        void find_user_by_id_success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            User result = userService.findById(1L);

            // then
            assertThat(result).isEqualTo(testUser);
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 사용자 조회 시 예외 발생")
        void find_user_by_nonexistent_id_throws_exception() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("이메일로 사용자 조회 성공")
        void find_user_by_email_success() {
            // given
            given(userRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(testUser));

            // when
            User result = userService.findByEmail("test@example.com");

            // then
            assertThat(result).isEqualTo(testUser);
            verify(userRepository).findByEmail("test@example.com");
        }
    }

    @Nested
    @DisplayName("이메일 관련 테스트")
    class EmailRelatedTest {

        @Test
        @DisplayName("사용 가능한 이메일 확인 성공")
        void check_email_available_success() {
            // given
            given(userRepository.existsByEmail("new@example.com")).willReturn(false);

            // when
            boolean result = userService.isEmailAvailable("new@example.com");

            // then
            assertThat(result).isTrue();
            verify(userRepository).existsByEmail("new@example.com");
        }

        @Test
        @DisplayName("이메일 중복 검증 - 사용 가능한 이메일")
        void validate_email_not_exists_success() {
            // given
            given(userRepository.existsByEmail("new@example.com")).willReturn(false);

            // when & then
            assertThatCode(() -> userService.validateEmailNotExists("new@example.com"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("이메일 중복 검증 - 이미 사용 중인 이메일로 예외 발생")
        void validate_email_not_exists_throws_exception_when_exists() {
            // given
            given(userRepository.existsByEmail("existing@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.validateEmailNotExists("existing@example.com"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("이메일 회원가입 테스트")
    class EmailSignupTest {

        @Test
        @DisplayName("이메일 회원가입 성공")
        void create_email_user_success() {
            // given
            given(userRepository.existsByEmail(testSignupRequest.getEmail())).willReturn(false);
            given(passwordEncoder.encode(testSignupRequest.getPassword())).willReturn("encoded_password");
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // when
            User result = userService.createEmailUser(testSignupRequest);

            // then
            assertThat(result).isEqualTo(testUser);
            verify(userRepository).existsByEmail(testSignupRequest.getEmail());
            verify(passwordEncoder).encode(testSignupRequest.getPassword());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("중복된 이메일로 회원가입 시 예외 발생")
        void create_email_user_with_duplicate_email_throws_exception() {
            // given
            given(userRepository.existsByEmail(testSignupRequest.getEmail())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createEmailUser(testSignupRequest))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("OAuth 회원가입 테스트")
    class OAuthSignupTest {

        @Test
        @DisplayName("OAuth 추가 회원가입 성공")
        void complete_oauth_signup_success() {
            // given
            given(accountService.findById(1L)).willReturn(testOAuthAccount);
            given(userRepository.existsByEmail(testSignupRequest.getEmail())).willReturn(false);
            given(passwordEncoder.encode(testSignupRequest.getPassword())).willReturn("encoded_password");
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // when
            User result = userService.completeOAuthSignup(1L, testSignupRequest);

            // then
            assertThat(result).isEqualTo(testUser);
            verify(accountService).findById(1L);
            verify(accountService).linkWithUser(1L, testUser);
        }

        @Test
        @DisplayName("OAuth 회원가입 시 중복 이메일로 예외 발생")
        void complete_oauth_signup_with_duplicate_email_throws_exception() {
            // given
            given(accountService.findById(1L)).willReturn(testOAuthAccount);
            given(userRepository.existsByEmail(testSignupRequest.getEmail())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.completeOAuthSignup(1L, testSignupRequest))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}
