package com.fitspine.repository;

import com.fitspine.model.FitbitSleepLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitbitSleepLogRepository extends JpaRepository<FitbitSleepLog, Long> {
    boolean existsByUserAndLogId(User user, Long longId);
}
