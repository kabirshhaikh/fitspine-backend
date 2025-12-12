package com.fitspine.repository;

import com.fitspine.model.ManualDailyLog;
import com.fitspine.model.ManualDailyPainLocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualDailyPainLocationLogRepository extends JpaRepository<ManualDailyPainLocationLog, Long> {
    void deleteByManualDailyLog(ManualDailyLog manualDailyLog);

    List<ManualDailyPainLocationLog> findByManualDailyLog(ManualDailyLog manualDailyLog);
}
