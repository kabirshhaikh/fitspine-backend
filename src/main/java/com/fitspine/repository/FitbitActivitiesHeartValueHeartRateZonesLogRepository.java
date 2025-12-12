package com.fitspine.repository;

import com.fitspine.model.FitbitActivitiesHeartValueHeartRateZonesLog;
import com.fitspine.model.FitbitActivitiesHeartValueLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FitbitActivitiesHeartValueHeartRateZonesLogRepository extends JpaRepository<FitbitActivitiesHeartValueHeartRateZonesLog, Long> {
    void deleteByFitbitActivitiesHeartValuesLog(FitbitActivitiesHeartValueLog valueLog);

}
