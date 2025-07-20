package com.swyp10.domain.activitylog.repository;

import com.swyp10.domain.activitylog.entity.UserActivityLog;
import com.swyp10.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserActivityLogRepository 테스트")
class UserActivityLogRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private UserActivityLogRepository activityLogRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .email("log@example.com")
            .password("secure")
            .nickname("로그유저")
            .signupCompleted(true)
            .build();
        entityManager.persist(user);
    }

    @Test
    @DisplayName("활동 로그 저장 및 조회")
    void save_and_find() {
        // given
        UserActivityLog log = UserActivityLog.builder()
            .user(user)
            .activityType("BOOKMARK")
            .targetType("Festival")
            .targetId(101L)
            .build();
        UserActivityLog saved = activityLogRepository.save(log);
        entityManager.flush();

        // when
        Optional<UserActivityLog> found = activityLogRepository.findById(saved.getLogId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getActivityType()).isEqualTo("BOOKMARK");
        assertThat(found.get().getUser().getEmail()).isEqualTo("log@example.com");
    }

    @Test
    @DisplayName("활동 로그 삭제")
    void delete() {
        // given
        UserActivityLog log = UserActivityLog.builder()
            .user(user)
            .activityType("REVIEW")
            .targetType("Festival")
            .targetId(102L)
            .build();
        activityLogRepository.save(log);
        entityManager.flush();

        // when
        activityLogRepository.delete(log);
        entityManager.flush();

        // then
        assertThat(activityLogRepository.findAll()).isEmpty();
    }
}
