package com.fitspine.repository;

import com.fitspine.model.ManualDailyLog;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManualDailyLogRepository extends JpaRepository<ManualDailyLog, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);


    Optional<ManualDailyLog> findByUserAndLogDate(User user, LocalDate logDate);

    List<ManualDailyLog> findByUserAndLogDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
