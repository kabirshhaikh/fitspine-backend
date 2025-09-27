package com.fitspine.repository;

import com.fitspine.model.ManualDailyLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ManualDailyLogRepository extends JpaRepository<ManualDailyLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);
}
