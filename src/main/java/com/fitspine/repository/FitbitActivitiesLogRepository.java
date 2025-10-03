package com.fitspine.repository;

import com.fitspine.model.FitbitActivitiesLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FitbitActivitiesLogRepository extends JpaRepository<FitbitActivitiesLog, Long> {
    boolean existsByUserAndLogId(User user, Long logId);

    Optional<FitbitActivitiesLog> findByUserAndLogDate(User user, LocalDate logDate);

}
