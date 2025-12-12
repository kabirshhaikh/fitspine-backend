package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.AiDailyInsightInterventionsToday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiDailyInsightInterventionsTodayRepository extends JpaRepository<AiDailyInsightInterventionsToday, Long> {
    void deleteByAiDailyInsight(AiDailyInsight insight);
}
