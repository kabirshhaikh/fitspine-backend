package com.fitspine.repository;

import com.fitspine.model.FitbitSleepSummaryLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface FitbitSleepSummaryLogRepository extends JpaRepository<FitbitSleepSummaryLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);
}
