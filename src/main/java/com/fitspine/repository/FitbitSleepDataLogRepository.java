package com.fitspine.repository;

import com.fitspine.model.FitbitSleepDataLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitbitSleepDataLogRepository extends JpaRepository<FitbitSleepDataLog, Long> {
}
