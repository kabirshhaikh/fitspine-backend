package com.fitspine.service.impl;

import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.enums.MorningStiffness;
import com.fitspine.enums.SittingTime;
import com.fitspine.enums.StandingTime;
import com.fitspine.enums.StressLevel;
import com.fitspine.exception.ResourceNotFoundException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.AiDailyAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

        //Manual Log
        ManualDailyLog manualDailyLog = manualDailyLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

        //Heart Log:
        FitbitActivitiesHeartLog heartLog = activitiesHeartLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
        Integer restingHeartRate = null;
        if (heartLog.getValues() != null && !heartLog.getValues().isEmpty()) {
            restingHeartRate = heartLog.getValues().get(0).getRestingHeartRate();
        }

        //Activity Log:
        FitbitActivitySummariesLog activitySummariesLog = activitySummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
        FitbitActivityGoalsLog activityGoalsLog = activityGoalsLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
        FitbitActivitiesLog activitiesLog = activityLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

        //Sleep log:
        FitbitSleepSummaryLog sleepSummaryLog = sleepSummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
        FitbitSleepLog sleepLog = sleepLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

        return AiUserDailyInputDto.builder()
                .id(user.getId())
                .logDate(logDate)

                // Manual
                .painLevel(manualDailyLog != null ? manualDailyLog.getPainLevel() : null)
                .flareUpToday(manualDailyLog != null ? manualDailyLog.getFlareUpToday() : null)
                .numbnessTingling(manualDailyLog != null ? manualDailyLog.getNumbnessTingling() : null)
                .sittingTime(manualDailyLog != null ? manualDailyLog.getSittingTime() : null)
                .standingTime(manualDailyLog != null ? manualDailyLog.getStandingTime() : null)
                .stretchingDone(manualDailyLog != null ? manualDailyLog.getStretchingDone() : null)
                .morningStiffness(manualDailyLog != null ? manualDailyLog.getMorningStiffness() : null)
                .stressLevel(manualDailyLog != null ? manualDailyLog.getStressLevel() : null)
                .liftingOrStrain(manualDailyLog != null ? manualDailyLog.getLiftingOrStrain() : null)
                .notes(manualDailyLog != null ? manualDailyLog.getNotes() : null)

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
