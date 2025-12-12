package com.fitspine.repository;

import com.fitspine.model.FitbitActivitiesHeartLog;
import com.fitspine.model.FitbitActivitiesHeartValueLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FitbitActivitiesHeartLogRepository extends JpaRepository<FitbitActivitiesHeartLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);

    Optional<FitbitActivitiesHeartLog> findByUserAndLogDate(User user, LocalDate logDate);

    List<FitbitActivitiesHeartLog> findByUserAndLogDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
