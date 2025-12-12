package com.fitspine.repository;

import com.fitspine.model.FitbitSleepDataLog;
import com.fitspine.model.FitbitSleepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FitbitSleepDataLogRepository extends JpaRepository<FitbitSleepDataLog, Long> {
    void deleteByFitbitSleepLog(FitbitSleepLog fitbitSleepLog);
}
