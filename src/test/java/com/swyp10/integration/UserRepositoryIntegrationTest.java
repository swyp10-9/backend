package com.swyp10.integration;

import com.swyp10.entity.User;
import com.swyp10.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@IntegrationTest
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 저장 및 조회 - 전체 플로우")
    void saveAndFindUser_FullFlow() {
        // given
        User user = User.builder()
                .email("integration@test.com")
                .password("encodedPassword")
                .nickname("통합테스트사용자")
                .signupCompleted(true)
                .build();

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        // 조회 검증
        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("integration@test.com");
        assertThat(foundUser.get().getNickname()).isEqualTo("통합테스트사용자");
    }

    @Test
    @DisplayName("이메일 중복 제약 조건 검증")
    void emailUniqueConstraint_Validation() {
        // given
        User user1 = User.builder()
                .email("duplicate@test.com")
                .password("password1")
                .nickname("사용자1")
                .signupCompleted(true)
                .build();

        User user2 = User.builder()
                .email("duplicate@test.com") // 같은 이메일
                .password("password2")
                .nickname("사용자2")
                .signupCompleted(true)
                .build();

        // when & then
        userRepository.save(user1);
        
        // 중복 이메일로 저장 시도 시 예외 발생
        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail_Check() {
        // given
        User user = User.builder()
                .email("exists@test.com")
                .password("password")
                .nickname("존재테스트")
                .signupCompleted(true)
                .build();

        userRepository.save(user);

        // when & then
        assertThat(userRepository.existsByEmail("exists@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@test.com")).isFalse();
    }

    @Test
    @DisplayName("사용자 정보 업데이트")
    void updateUser_Success() throws InterruptedException {
        // given
        User user = User.builder()
                .email("update@test.com")
                .password("oldPassword")
                .nickname("이전닉네임")
                .signupCompleted(false)
                .build();

        User savedUser = userRepository.save(user);
        userRepository.flush(); // ✅ 강제 플러시

        LocalDateTime createdTime = savedUser.getCreatedAt();
        Thread.sleep(100);

        // when
        savedUser.setNickname("새로운닉네임");
        savedUser.setSignupCompleted(true);
        User updatedUser = userRepository.save(savedUser);
        userRepository.flush(); // ✅ 강제 플러시

        // then

        assertThat(updatedUser.getNickname()).isEqualTo("새로운닉네임");
        assertThat(updatedUser.getSignupCompleted()).isTrue();
        assertThat(updatedUser.getUpdatedAt()).isAfter(createdTime);
    }
}
