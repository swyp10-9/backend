package com.swyp10.repository;

import com.swyp10.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트사용자")
                .signupCompleted(true)
                .build();
    }

    @Test
    @DisplayName("사용자 저장 및 조회")
    void save_And_FindById() {
        // given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findById(savedUser.getUserId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getNickname()).isEqualTo("테스트사용자");
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 존재하는 경우")
    void findByEmail_Exists() {
        // given
        userRepository.save(testUser);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getNickname()).isEqualTo("테스트사용자");
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 존재하지 않는 경우")
    void findByEmail_NotExists() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 존재하는 경우")
    void existsByEmail_Exists() {
        // given
        userRepository.save(testUser);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 존재하지 않는 경우")
    void existsByEmail_NotExists() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("여러 사용자 저장 및 조회")
    void saveMultipleUsers() {
        // given
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password1")
                .nickname("사용자1")
                .signupCompleted(true)
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .password("password2")
                .nickname("사용자2")
                .signupCompleted(true)
                .build();

        // when
        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();

        // then
        assertThat(userRepository.findByEmail("user1@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user2@example.com")).isPresent();
        assertThat(userRepository.existsByEmail("user1@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("user2@example.com")).isTrue();
    }

    @Test
    @DisplayName("사용자 삭제")
    void deleteUser() {
        // given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // when
        userRepository.delete(savedUser);
        entityManager.flush();

        // then
        assertThat(userRepository.findById(savedUser.getUserId())).isNotPresent();
        assertThat(userRepository.existsByEmail("test@example.com")).isFalse();
    }
}
