package com.fitspine.repository;

import com.fitspine.model.FitbitActivitySummariesDistancesLog;
import com.fitspine.model.FitbitActivitySummariesLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FitbitActivitySummariesDistancesLogRepository extends JpaRepository<FitbitActivitySummariesDistancesLog, Long> {
    void deleteByFitbitActivitySummariesLog(FitbitActivitySummariesLog summaryLog);
}
