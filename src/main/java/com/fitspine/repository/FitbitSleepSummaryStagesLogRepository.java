package com.fitspine.repository;

import com.fitspine.model.FitbitSleepSummaryLog;
import com.fitspine.model.FitbitSleepSummaryStagesLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FitbitSleepSummaryStagesLogRepository extends JpaRepository<FitbitSleepSummaryStagesLog, Long> {
    void deleteByFitbitSleepSummaryLog(FitbitSleepSummaryLog sleepSummaryLog);
}
