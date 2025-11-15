package com.fitspine.repository;

import com.fitspine.model.FitbitSleepLog;
import com.fitspine.model.FitbitSleepShortDataLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitbitSleepShortDataLogRepository extends JpaRepository<FitbitSleepShortDataLog, Long> {
    void deleteByFitbitSleepLog(FitbitSleepLog fitbitSleepLog);
}
