package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.AiDailyInsightImproved;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDailyInsightImprovedRepository extends JpaRepository<AiDailyInsightImproved, Long> {
    void deleteByAiDailyInsight(AiDailyInsight insight);
}
