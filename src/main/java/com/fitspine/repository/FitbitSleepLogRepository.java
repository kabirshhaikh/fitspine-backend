package com.fitspine.repository;

import com.fitspine.model.FitbitSleepLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitbitSleepLogRepository extends JpaRepository<FitbitSleepLog, Long> {
}
