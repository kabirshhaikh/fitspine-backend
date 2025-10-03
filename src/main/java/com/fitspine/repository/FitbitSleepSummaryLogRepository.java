package com.fitspine.repository;

import com.fitspine.model.FitbitSleepSummaryLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FitbitSleepSummaryLogRepository extends JpaRepository<FitbitSleepSummaryLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);

    Optional<FitbitSleepSummaryLog> findByUserAndLogDate(User user, LocalDate logDate);
}
