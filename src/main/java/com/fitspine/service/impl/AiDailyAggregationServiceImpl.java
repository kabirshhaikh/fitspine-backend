package com.fitspine.service.impl;

import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.enums.WearableType;
import com.fitspine.exception.ManualDailyLogNotFoundException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.AiDailyAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class AiDailyAggregationServiceImpl implements AiDailyAggregationService {
    private final UserRepository userRepository;
    private final ManualDailyLogRepository manualDailyLogRepo;
    private final FitbitActivitiesHeartLogRepository activitiesHeartLogRepo;
    private final FitbitActivitySummariesLogRepository activitySummaryLogRepo;
    private final FitbitActivityGoalsLogRepository activityGoalsLogRepo;
    private final FitbitActivitiesLogRepository activityLogRepo;
    private final FitbitSleepSummaryLogRepository sleepSummaryLogRepo;
    private final FitbitSleepLogRepository sleepLogRepo;


    public AiDailyAggregationServiceImpl(
            UserRepository userRepository,
            ManualDailyLogRepository manualDailyLogRepo,
            FitbitActivitiesHeartLogRepository activitiesHeartLogRepo,
            FitbitActivitySummariesLogRepository activitySummaryLogRepo,
            FitbitActivityGoalsLogRepository activityGoalsLogRepo,
            FitbitActivitiesLogRepository activityLogRepo,
            FitbitSleepSummaryLogRepository sleepSummaryLogRepo,
            FitbitSleepLogRepository sleepLogRepo
    ) {
        this.userRepository = userRepository;
        this.manualDailyLogRepo = manualDailyLogRepo;
        this.activitiesHeartLogRepo = activitiesHeartLogRepo;
        this.activitySummaryLogRepo = activitySummaryLogRepo;
        this.activityGoalsLogRepo = activityGoalsLogRepo;
        this.activityLogRepo = activityLogRepo;
        this.sleepSummaryLogRepo = sleepSummaryLogRepo;
        this.sleepLogRepo = sleepLogRepo;
    }

    @Override
    public AiUserDailyInputDto buildAiInput(String email, LocalDate logDate) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email:" + email));

        //Manual log:
        ManualDailyLog manualDailyLog = manualDailyLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

        //If manual log is not found throw this error, so that the user creates at least manual log before requesting insights and disc score:
        if (manualDailyLog == null) {
            log.warn("No manual log found for user {} on date {}", user.getId(), logDate);
            throw new ManualDailyLogNotFoundException("User " + user.getId() + " does not have manual log for date:" + logDate);
        }

        //Heart log:
        FitbitActivitiesHeartLog heartLog = null;
        Integer restingHeartRate = null;

        //Activity Log:
        FitbitActivitySummariesLog activitySummariesLog = null;
        FitbitActivityGoalsLog activityGoalsLog = null;
        FitbitActivitiesLog activitiesLog = null;

        //Sleep log:
        FitbitSleepSummaryLog sleepSummaryLog = null;
        FitbitSleepLog sleepLog = null;

        boolean hasFitbitConnection = Boolean.TRUE.equals(user.getIsWearableConnected()) && user.getWearableType() != null && user.getWearableType() == WearableType.FITBIT;

        if (hasFitbitConnection) {
            log.info("User {} has Fitbit connected. Loading fitbit data for date {}:", user.getId(), logDate);

            //Heart:
            heartLog = activitiesHeartLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            if (heartLog != null && heartLog.getValues() != null && !heartLog.getValues().isEmpty()) {
                restingHeartRate = heartLog.getValues().get(0).getRestingHeartRate();
            }

            //Activity:
            activitySummariesLog = activitySummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            activityGoalsLog = activityGoalsLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            activitiesLog = activityLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

            //Sleep:
            sleepSummaryLog = sleepSummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            sleepLog = sleepLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
        } else {
            log.info("User {} does not have fitbit connected. Skipping Fitbit data fetch for log date {}:", user.getId(), logDate);
        }


        return AiUserDailyInputDto.builder()
                .id(user.getId())
                .logDate(logDate)

                // Manual
                .painLevel(manualDailyLog.getPainLevel())
                .flareUpToday(manualDailyLog.getFlareUpToday())
                .numbnessTingling(manualDailyLog.getNumbnessTingling())
                .sittingTime(manualDailyLog.getSittingTime())
                .standingTime(manualDailyLog.getStandingTime())
                .stretchingDone(manualDailyLog.getStretchingDone())
                .morningStiffness(manualDailyLog.getMorningStiffness())
                .stressLevel(manualDailyLog.getStressLevel())
                .liftingOrStrain(manualDailyLog.getLiftingOrStrain())
                .notes(manualDailyLog.getNotes())

                // Heart
                .restingHeartRate(restingHeartRate)

                // Activity summary
                .caloriesOut(activitySummariesLog != null ? activitySummariesLog.getCaloriesOut() : null)
                .activityCalories(activitySummariesLog != null ? activitySummariesLog.getActivityCalories() : null)
                .caloriesBmr(activitySummariesLog != null ? activitySummariesLog.getCaloriesBmr() : null)
                .activeScore(activitySummariesLog != null ? activitySummariesLog.getActiveScore() : null)
                .steps(activitySummariesLog != null ? activitySummariesLog.getSteps() : null)
                .sedentaryMinutes(activitySummariesLog != null ? activitySummariesLog.getSedentaryMinutes() : null)
                .lightlyActiveMinutes(activitySummariesLog != null ? activitySummariesLog.getLightlyActiveMinutes() : null)
                .fairlyActiveMinutes(activitySummariesLog != null ? activitySummariesLog.getFairlyActiveMinutes() : null)
                .veryActiveMinutes(activitySummariesLog != null ? activitySummariesLog.getVeryActiveMinutes() : null)
                .marginalCalories(activitySummariesLog != null ? activitySummariesLog.getMarginalCalories() : null)

                // Goals
                .floors(activityGoalsLog != null ? activityGoalsLog.getFloors() : null)
                .activeMinutes(activityGoalsLog != null ? activityGoalsLog.getActiveMinutes() : null)

                // Activities
                .description(activitiesLog != null ? activitiesLog.getDescription() : null)

                // Sleep summary
                .totalMinutesAsleep(sleepSummaryLog != null ? sleepSummaryLog.getTotalMinutesAsleep() : null)
                .totalSleepRecords(sleepSummaryLog != null ? sleepSummaryLog.getTotalSleepRecords() : null)
                .totalTimeInBed(sleepSummaryLog != null ? sleepSummaryLog.getTotalTimeInBed() : null)

                // Sleep log
                .efficiency(sleepLog != null ? sleepLog.getEfficiency() : null)
                .startTime(sleepLog != null ? sleepLog.getStartTime() : null)
                .endTime(sleepLog != null ? sleepLog.getEndTime() : null)
                .infoCode(sleepLog != null ? sleepLog.getInfoCode() : null)
                .isMainSleep(sleepLog != null ? sleepLog.getIsMainSleep() : null)
                .minutesAfterWakeup(sleepLog != null ? sleepLog.getMinutesAfterWakeup() : null)
                .minutesAwake(sleepLog != null ? sleepLog.getMinutesAwake() : null)
                .minutesAsleep(sleepLog != null ? sleepLog.getMinutesAsleep() : null)
                .minutesToFallAsleep(sleepLog != null ? sleepLog.getMinutesToFallAsleep() : null)
                .logType(sleepLog != null ? sleepLog.getLogType() : null)
                .timeInBed(sleepLog != null ? sleepLog.getTimeInBed() : null)
                .type(sleepLog != null ? sleepLog.getType() : null)

                .build();
    }
}
