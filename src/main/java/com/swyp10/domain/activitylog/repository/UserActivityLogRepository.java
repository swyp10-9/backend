package com.swyp10.domain.activitylog.repository;

import com.swyp10.domain.activitylog.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
}
