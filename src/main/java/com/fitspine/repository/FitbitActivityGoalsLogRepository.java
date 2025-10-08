package com.fitspine.repository;

import com.fitspine.model.FitbitActivityGoalsLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FitbitActivityGoalsLogRepository extends JpaRepository<FitbitActivityGoalsLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);

    Optional<FitbitActivityGoalsLog> findByUserAndLogDate(User user, LocalDate logDate);

    List<FitbitActivityGoalsLog> findByUserAndLogDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
