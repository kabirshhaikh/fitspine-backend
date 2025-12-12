package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.AiDailyInsightActionableAdvice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiDailyInsightActionableAdviceRepository extends JpaRepository<AiDailyInsightActionableAdvice, Long> {
    void deleteByAiDailyInsight(AiDailyInsight insight);
}
