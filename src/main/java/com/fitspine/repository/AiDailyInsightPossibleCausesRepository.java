package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.AiDailyInsightPossibleCauses;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDailyInsightPossibleCausesRepository extends JpaRepository<AiDailyInsightPossibleCauses, Long> {
    void deleteByAiDailyInsight(AiDailyInsight insight);
}
