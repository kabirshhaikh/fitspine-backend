package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AiDailyInsightRepository extends JpaRepository<AiDailyInsight, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);
}
