package com.fitspine.repository;

import com.fitspine.model.FitbitActivitiesHeartLog;
import com.fitspine.model.FitbitActivitiesHeartValueLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FitbitActivitiesHeartLogRepository extends JpaRepository<FitbitActivitiesHeartLog, Long> {
    boolean existsByUserAndDateTime(User user, LocalDate logDate);

    Optional<FitbitActivitiesHeartLog> findByUserAndLogDate(User user, LocalDate logDate);
}
