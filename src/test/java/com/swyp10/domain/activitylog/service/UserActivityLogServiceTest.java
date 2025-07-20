package com.swyp10.domain.activitylog.service;

import com.swyp10.domain.activitylog.entity.UserActivityLog;
import com.swyp10.domain.activitylog.repository.UserActivityLogRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserActivityLogService 테스트")
class UserActivityLogServiceTest {

    @Mock private UserActivityLogRepository activityLogRepository;
    @InjectMocks private UserActivityLogService activityLogService;

    private UserActivityLog testLog;

    @BeforeEach
    void setUp() {
        testLog = UserActivityLog.builder()
            .logId(1L)
            .activityType("REVIEW")
            .targetType("Festival")
            .targetId(999L)
            .build();
    }

    @Nested
    @DisplayName("활동 로그 조회")
    class GetLog {

        @Test
        @DisplayName("활동 로그 조회 성공")
        void get_success() {
            given(activityLogRepository.findById(1L)).willReturn(Optional.of(testLog));

            UserActivityLog result = activityLogService.getUserActivityLog(1L);

            assertThat(result).isEqualTo(testLog);
            verify(activityLogRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 활동 로그 조회 시 예외 발생")
        void get_not_found() {
            given(activityLogRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> activityLogService.getUserActivityLog(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("활동 로그 생성 성공")
    void create_success() {
        given(activityLogRepository.save(testLog)).willReturn(testLog);

        UserActivityLog result = activityLogService.createUserActivityLog(testLog);

        assertThat(result).isEqualTo(testLog);
        verify(activityLogRepository).save(testLog);
    }

    @Test
    @DisplayName("활동 로그 삭제 성공")
    void delete_success() {
        willDoNothing().given(activityLogRepository).deleteById(1L);

        activityLogService.deleteUserActivityLog(1L);

        verify(activityLogRepository).deleteById(1L);
    }
}
