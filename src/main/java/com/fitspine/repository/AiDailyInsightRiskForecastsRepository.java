package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.AiDailyInsightRiskForecasts;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AiDailyInsightRiskForecastsRepository extends JpaRepository<AiDailyInsightRiskForecasts, Long> {
    void deleteByAiDailyInsight(AiDailyInsight insight);

    int countByAiDailyInsight_UserAndAiDailyInsight_LogDate(User user, LocalDate logDate);

}
