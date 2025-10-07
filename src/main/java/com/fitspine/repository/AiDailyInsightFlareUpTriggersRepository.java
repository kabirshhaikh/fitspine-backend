package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.AiDailyInsightFlareUpTriggers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiDailyInsightFlareUpTriggersRepository extends JpaRepository<AiDailyInsightFlareUpTriggers, Long> {
    List<AiDailyInsightFlareUpTriggers> findByAiDailyInsight(AiDailyInsight aiDailyInsight);

    void deleteByAiDailyInsight(AiDailyInsight aiDailyInsight);
}
