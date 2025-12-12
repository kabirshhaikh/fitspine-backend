package com.fitspine.repository;

import com.fitspine.model.FitbitActivitySummariesLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FitbitActivitySummariesLogRepository extends JpaRepository<FitbitActivitySummariesLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);

    Optional<FitbitActivitySummariesLog> findByUserAndLogDate(User user, LocalDate logDate);

    List<FitbitActivitySummariesLog> findByUserAndLogDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
