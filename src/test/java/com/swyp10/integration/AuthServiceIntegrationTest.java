package com.swyp10.integration;

import com.swyp10.domain.auth.dto.common.LoginRequest;
import com.swyp10.domain.auth.dto.common.SignupRequest;
import com.swyp10.domain.auth.dto.common.TokenResponse;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.UserRepository;
import com.swyp10.domain.auth.service.common.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@IntegrationTest
@DisplayName("인증 서비스 통합 테스트")
class AuthServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        signupRequest = SignupRequest.builder()
                .email("integration-test@example.com")
                .password("password123")
                .nickname("통합테스트사용자")
                .build();

        loginRequest = LoginRequest.builder()
                .email("integration-test@example.com")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("이메일 회원가입 후 로그인 - 전체 플로우 테스트")
    void signupAndLogin_IntegrationTest() {
        // 1. 회원가입
        TokenResponse signupResponse = emailService.signup(signupRequest);

        assertThat(signupResponse).isNotNull();
        assertThat(signupResponse.getAccessToken()).isNotNull();
        assertThat(signupResponse.getNickname()).isEqualTo("통합테스트사용자");

        // 2. 데이터베이스에 사용자가 생성되었는지 확인
        User savedUser = userRepository.findByEmail("integration-test@example.com")
                .orElseThrow(() -> new AssertionError("사용자가 생성되지 않았습니다."));

        assertThat(savedUser.getEmail()).isEqualTo("integration-test@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("통합테스트사용자");
        assertThat(savedUser.getSignupCompleted()).isTrue();

        // 3. 로그인
        TokenResponse loginResponse = emailService.login(loginRequest);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getAccessToken()).isNotNull();
        assertThat(loginResponse.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(loginResponse.getNickname()).isEqualTo("통합테스트사용자");
    }

    @Test
    @DisplayName("이메일 중복 확인 테스트")
    void checkEmailAvailability_IntegrationTest() {
        // 1. 초기 상태 - 사용 가능한 이메일
        boolean availableBeforeSignup = emailService.isEmailAvailable("integration-test@example.com");
        assertThat(availableBeforeSignup).isTrue();

        // 2. 회원가입 후 - 사용 불가능한 이메일
        emailService.signup(signupRequest);
        boolean availableAfterSignup = emailService.isEmailAvailable("integration-test@example.com");
        assertThat(availableAfterSignup).isFalse();

        // 3. 다른 이메일 - 사용 가능한 이메일
        boolean anotherEmailAvailable = emailService.isEmailAvailable("another-email@example.com");
        assertThat(anotherEmailAvailable).isTrue();
    }
}
