package com.fitspine.repository;

import com.fitspine.model.ManualDailyLog;
import com.fitspine.model.ManualDailyPainLocationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualDailyPainLocationLogRepository extends JpaRepository<ManualDailyPainLocationLog, Long> {
    void deleteByManualDailyLog(ManualDailyLog manualDailyLog);
}
