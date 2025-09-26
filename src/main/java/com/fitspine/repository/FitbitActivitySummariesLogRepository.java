package com.fitspine.repository;

import com.fitspine.model.FitbitActivitySummariesLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface FitbitActivitySummariesLogRepository extends JpaRepository<FitbitActivitySummariesLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);
}
