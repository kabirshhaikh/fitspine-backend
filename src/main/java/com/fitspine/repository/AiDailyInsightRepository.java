package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AiDailyInsightRepository extends JpaRepository<AiDailyInsight, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);

    Optional<AiDailyInsight> findByUserAndLogDate(User user, LocalDate logDate);
}
