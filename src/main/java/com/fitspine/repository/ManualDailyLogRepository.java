package com.fitspine.repository;

import com.fitspine.model.ManualDailyLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ManualDailyLogRepository extends JpaRepository<ManualDailyLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);


    Optional<ManualDailyLog> findByUserAndLogDate(User user, LocalDate logDate);
}
