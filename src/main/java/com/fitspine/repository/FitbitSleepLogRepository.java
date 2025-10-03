package com.fitspine.repository;

import com.fitspine.model.FitbitSleepLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FitbitSleepLogRepository extends JpaRepository<FitbitSleepLog, Long> {
    boolean existsByUserAndLogId(User user, Long longId);

    Optional<FitbitSleepLog> findByUserAndLogDate(User user, LocalDate logDate);
}
