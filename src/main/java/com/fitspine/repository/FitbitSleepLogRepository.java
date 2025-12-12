package com.fitspine.repository;

import com.fitspine.model.FitbitActivitySummariesLog;
import com.fitspine.model.FitbitSleepLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FitbitSleepLogRepository extends JpaRepository<FitbitSleepLog, Long> {
    boolean existsByUserAndLogId(User user, Long longId);

    Optional<FitbitSleepLog> findByUserAndLogDate(User user, LocalDate logDate);

    Optional<FitbitSleepLog> findByUserAndLogId(User user, Long logId);

    List<FitbitSleepLog> findByUserAndLogDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
