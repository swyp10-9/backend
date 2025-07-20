package com.swyp10.domain.activitylog.entity;

import com.swyp10.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserActivityLog Entity 테스트")
class UserActivityLogTest {

    @Test
    @DisplayName("UserActivityLog 엔티티 생성")
    void createActivityLog() {
        User user = User.builder()
            .userId(1L)
            .email("activity@test.com")
            .nickname("로그유저")
            .build();

        UserActivityLog log = UserActivityLog.builder()
            .activityType("BOOKMARK")
            .targetType("FESTIVAL")
            .targetId(123L)
            .user(user)
            .build();

        log.onCreate(); // PrePersist 수동 실행

        assertThat(log.getUser()).isEqualTo(user);
        assertThat(log.getActivityType()).isEqualTo("BOOKMARK");
        assertThat(log.getTargetType()).isEqualTo("FESTIVAL");
        assertThat(log.getTargetId()).isEqualTo(123L);
        assertThat(log.getCreatedAt()).isNotNull();
    }
}
