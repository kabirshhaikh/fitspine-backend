package com.fitspine.repository;

import com.fitspine.model.FitbitActivitiesLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FitbitActivitiesLogRepository extends JpaRepository<FitbitActivitiesLog, Long> {
    boolean existsByUserAndLogId(User user, Long logId);

    List<FitbitActivitiesLog> findByUserAndLogDate(User user, LocalDate logDate);

    Optional<FitbitActivitiesLog> findByUserAndLogId(User user, Long logId);
}
