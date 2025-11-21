package com.fitspine.repository;

import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.AiDailyInsightWorsened;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDailyInsightWorsenedRepository extends JpaRepository<AiDailyInsightWorsened, Long> {
    void deleteByAiDailyInsight(AiDailyInsight insight);
}
