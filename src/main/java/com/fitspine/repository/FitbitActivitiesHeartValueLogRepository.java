package com.fitspine.repository;

import com.fitspine.model.FitbitActivitiesHeartLog;
import com.fitspine.model.FitbitActivitiesHeartValueLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FitbitActivitiesHeartValueLogRepository extends JpaRepository<FitbitActivitiesHeartValueLog, Long> {
    FitbitActivitiesHeartValueLog findByFitbitActivitiesHeartLog(FitbitActivitiesHeartLog heartLog);
}
