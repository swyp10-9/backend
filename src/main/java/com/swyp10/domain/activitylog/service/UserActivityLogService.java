package com.swyp10.domain.activitylog.service;

import com.swyp10.domain.activitylog.entity.UserActivityLog;
import com.swyp10.domain.activitylog.repository.UserActivityLogRepository;
import com.swyp10.global.exception.ApplicationException;
import com.swyp10.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityLogService {

    private final UserActivityLogRepository activityLogRepository;

    public UserActivityLog getUserActivityLog(Long logId) {
        return activityLogRepository.findById(logId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "UserActivityLog not found: " + logId));
    }

    @Transactional
    public UserActivityLog createUserActivityLog(UserActivityLog userActivityLog) {
        return activityLogRepository.save(userActivityLog);
    }

    @Transactional
    public void deleteUserActivityLog(Long logId) {
        activityLogRepository.deleteById(logId);
    }

}
