package com.fitspine.service.impl;

import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.enums.*;
import com.fitspine.exception.ManualDailyLogNotFoundException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.EnumScoreHelper;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.FitbitAiDailyAggregationService;
import com.fitspine.service.FitbitApiClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FitbitAiDailyAggregationServiceImpl implements FitbitAiDailyAggregationService {
    private final UserRepository userRepository;
    private final ManualDailyLogRepository manualDailyLogRepo;
    private final FitbitActivitiesHeartLogRepository activitiesHeartLogRepo;
    private final FitbitActivitySummariesLogRepository activitySummaryLogRepo;
    private final FitbitActivityGoalsLogRepository activityGoalsLogRepo;
    private final FitbitActivitiesLogRepository activityLogRepo;
    private final FitbitSleepSummaryLogRepository sleepSummaryLogRepo;
    private final FitbitSleepLogRepository sleepLogRepo;
    private final FitbitApiClientService fitbitApiClientService;

    public FitbitAiDailyAggregationServiceImpl(
            UserRepository userRepository,
            ManualDailyLogRepository manualDailyLogRepo,
            FitbitActivitiesHeartLogRepository activitiesHeartLogRepo,
            FitbitActivitySummariesLogRepository activitySummaryLogRepo,
            FitbitActivityGoalsLogRepository activityGoalsLogRepo,
            FitbitActivitiesLogRepository activityLogRepo,
            FitbitSleepSummaryLogRepository sleepSummaryLogRepo,
            FitbitSleepLogRepository sleepLogRepo,
            FitbitApiClientService fitbitApiClientService
    ) {
        this.userRepository = userRepository;
        this.manualDailyLogRepo = manualDailyLogRepo;
        this.activitiesHeartLogRepo = activitiesHeartLogRepo;
        this.activitySummaryLogRepo = activitySummaryLogRepo;
        this.activityGoalsLogRepo = activityGoalsLogRepo;
        this.activityLogRepo = activityLogRepo;
        this.sleepSummaryLogRepo = sleepSummaryLogRepo;
        this.sleepLogRepo = sleepLogRepo;
        this.fitbitApiClientService = fitbitApiClientService;
    }

    @Override
    public AiUserDailyInputDto buildAiInput(String email, LocalDate logDate) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email:" + email));

        List<InjuryType> injuryTypes = user.getUserInjuryList()
                .stream()
                .map(UserInjury::getInjuryType)
                .toList();

        List<SurgeryType> surgeryTypes = user.getUserSurgeryList()
                .stream()
                .map(UserSurgery::getSurgeryType)
                .toList();

        List<DiscLevel> discLevels = user.getUserDiscIssueList()
                .stream()
                .map(UserDiscIssue::getDiscLevel)
                .toList();

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
            activitySummariesLog = activitySummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            sleepSummaryLog = sleepSummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

            if (heartLog == null && activitySummariesLog == null && sleepSummaryLog == null) {
                log.info("No Fitbit data found for {}. Triggering on-demand sync for {}", user.getId(), logDate);

                //Fetch data:
                try {
                    log.info("Running try block fetch activity, heart and sleep data on demand..");
                    fitbitApiClientService.getActivity(user.getEmail(), logDate.toString());
                    fitbitApiClientService.getHeartRate(user.getEmail(), logDate.toString());
                    fitbitApiClientService.getSleep(user.getEmail(), logDate.toString());
                    log.info("Fetching of data complete...");

                    //Re-fetch and sync the data:
                    heartLog = activitiesHeartLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
                    activitySummariesLog = activitySummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
                    sleepSummaryLog = sleepSummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
                } catch (Exception e) {
                    log.error("Failed to fetch on-demand Fitbit data for user {}: {}", user.getId(), e.getMessage());
                }
            }

            if (heartLog != null && heartLog.getValues() != null && !heartLog.getValues().isEmpty()) {
                restingHeartRate = heartLog.getValues().get(0).getRestingHeartRate();
            } else {
                restingHeartRate = -1;
            }

            //Activity:

            activityGoalsLog = activityGoalsLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            activitiesLog = activityLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

            //Sleep:
            sleepLog = sleepLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
        } else {
            log.info("User {} does not have fitbit connected. Skipping Fitbit data fetch for log date {}:", user.getId(), logDate);
        }


        return AiUserDailyInputDto.builder()
                .id(user.getId())
                .logDate(logDate)

                //User info:
                .gender(user.getGender())
                .age(user.getAge())
                .hasSurgeryHistory(user.getSurgeryHistory())
                .injuryTypes(injuryTypes)
                .surgeryTypes(surgeryTypes)
                .discLevels(discLevels)

                // Manual
                .painLevel(EnumScoreHelper.pain(manualDailyLog.getPainLevel()))
                .flareUpToday(manualDailyLog.getFlareUpToday())
                .numbnessTingling(manualDailyLog.getNumbnessTingling())
                .sittingTime(EnumScoreHelper.sittingTime(manualDailyLog.getSittingTime()))
                .standingTime(EnumScoreHelper.standingTime(manualDailyLog.getStandingTime()))
                .stretchingDone(manualDailyLog.getStretchingDone())
                .morningStiffness(EnumScoreHelper.morningStiffness(manualDailyLog.getMorningStiffness()))
                .stressLevel(EnumScoreHelper.stressLevel(manualDailyLog.getStressLevel()))
                .liftingOrStrain(manualDailyLog.getLiftingOrStrain())
                .notes(manualDailyLog.getNotes())

                // Heart
                .restingHeartRate(restingHeartRate)

                // Activity summary
                .caloriesOut(activitySummariesLog != null ? activitySummariesLog.getCaloriesOut() : -1)
                .activityCalories(activitySummariesLog != null ? activitySummariesLog.getActivityCalories() : -1)
                .caloriesBmr(activitySummariesLog != null ? activitySummariesLog.getCaloriesBmr() : -1)
                .steps(activitySummariesLog != null ? activitySummariesLog.getSteps() : -1)
                .sedentaryMinutes(activitySummariesLog != null ? activitySummariesLog.getSedentaryMinutes() : -1)
                .lightlyActiveMinutes(activitySummariesLog != null ? activitySummariesLog.getLightlyActiveMinutes() : -1)
                .fairlyActiveMinutes(activitySummariesLog != null ? activitySummariesLog.getFairlyActiveMinutes() : -1)
                .veryActiveMinutes(activitySummariesLog != null ? activitySummariesLog.getVeryActiveMinutes() : -1)
                .marginalCalories(activitySummariesLog != null ? activitySummariesLog.getMarginalCalories() : -1)

                // Goals
                .floors(activityGoalsLog != null ? activityGoalsLog.getFloors() : -1)
                .activeMinutes(activityGoalsLog != null ? activityGoalsLog.getActiveMinutes() : -1)

                // Activities
                .description(activitiesLog != null ? activitiesLog.getDescription() : null)

                // Sleep summary
                .totalMinutesAsleep(sleepSummaryLog != null ? sleepSummaryLog.getTotalMinutesAsleep() : -1)
                .totalSleepRecords(sleepSummaryLog != null ? sleepSummaryLog.getTotalSleepRecords() : -1)
                .totalTimeInBed(sleepSummaryLog != null ? sleepSummaryLog.getTotalTimeInBed() : -1)

                // Sleep log
                .efficiency(sleepLog != null ? sleepLog.getEfficiency() : -1)
                .startTime(sleepLog != null ? sleepLog.getStartTime() : null)
                .endTime(sleepLog != null ? sleepLog.getEndTime() : null)
                .isMainSleep(sleepLog != null ? sleepLog.getIsMainSleep() : null)
                .minutesAwake(sleepLog != null ? sleepLog.getMinutesAwake() : -1)
                .minutesAsleep(sleepLog != null ? sleepLog.getMinutesAsleep() : -1)
                .minutesToFallAsleep(sleepLog != null ? sleepLog.getMinutesToFallAsleep() : -1)
                .timeInBed(sleepLog != null ? sleepLog.getTimeInBed() : -1)

                .build();
    }
}
