package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AiDailyInsightRepository extends JpaRepository<AiDailyInsight, Long> {
    boolean existsByUserAndLogDate(User user, LocalDate logDate);

    Optional<AiDailyInsight> findByUserAndLogDate(User user, LocalDate logDate);

    @Query("""
                SELECT DISTINCT u.id
                FROM ManualDailyLog m
                JOIN m.user u
                LEFT JOIN AiDailyInsight i
                    ON i.user = u AND i.logDate = m.logDate
                WHERE m.logDate = :targetDate
                  AND i.id IS NULL
            """)
    List<Long> findUsersWithManualDailyLogsButNoInsights(@Param("targetDate") LocalDate targetDate);
}
