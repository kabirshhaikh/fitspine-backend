package com.fitspine.service.impl;

import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.enums.*;
import com.fitspine.exception.ManualDailyLogNotFoundException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.AiInsightHelper;
import com.fitspine.helper.DeIdentificationHelper;
import com.fitspine.helper.EnumScoreHelper;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.FitbitAiDailyAggregationService;
import com.fitspine.service.FitbitApiClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final DeIdentificationHelper deIdentificationHelper;
    private final AiInsightHelper helper;
    private final RedisTemplate<String, Object> redis;


    public FitbitAiDailyAggregationServiceImpl(
            UserRepository userRepository,
            ManualDailyLogRepository manualDailyLogRepo,
            FitbitActivitiesHeartLogRepository activitiesHeartLogRepo,
            FitbitActivitySummariesLogRepository activitySummaryLogRepo,
            FitbitActivityGoalsLogRepository activityGoalsLogRepo,
            FitbitActivitiesLogRepository activityLogRepo,
            FitbitSleepSummaryLogRepository sleepSummaryLogRepo,
            FitbitSleepLogRepository sleepLogRepo,
            FitbitApiClientService fitbitApiClientService,
            DeIdentificationHelper deIdentificationHelper,
            AiInsightHelper helper,
            RedisTemplate<String, Object> redis
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
        this.deIdentificationHelper = deIdentificationHelper;
        this.helper = helper;
        this.redis = redis;
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
            throw new ManualDailyLogNotFoundException("User " + user.getFullName() + " does not have manual log for date:" + logDate);
        }

        //Heart log:
        FitbitActivitiesHeartLog heartLog = null;
        Integer restingHeartRate = null;

        //Activity Log:
        FitbitActivitySummariesLog activitySummariesLog = null;
        FitbitActivityGoalsLog activityGoalsLog = null;
        List<FitbitActivitiesLog> activitiesLog = new ArrayList<>();

        //Sleep log:
        FitbitSleepSummaryLog sleepSummaryLog = null;
        FitbitSleepLog sleepLog = null;

        boolean hasFitbitConnection = Boolean.TRUE.equals(user.getIsWearableConnected()) && user.getWearableType() != null && user.getWearableType() == WearableType.FITBIT;

        if (hasFitbitConnection) {
            log.info("User {} has Fitbit connected. Refreshing fitbit data for date {}:", user.getId(), logDate);

            //Set rate limiting and for that set redis key:
            String key = "fitbit_last_sync:" + user.getPublicId();
            Duration timeToLive = Duration.ofMinutes(5);
            Boolean canSync = redis.opsForValue().setIfAbsent(key, String.valueOf(System.currentTimeMillis()), timeToLive);

            if (Boolean.TRUE.equals(canSync)) {
                log.info("Rate limit OK");
                log.info("First fitbit sync or Time To Live expired for user public ID: {} on date {}", user.getPublicId(), logDate);

                //Sync the data:
                try {
                    fitbitApiClientService.getActivity(user.getEmail(), logDate.toString());
                    fitbitApiClientService.getHeartRate(user.getEmail(), logDate.toString());
                    fitbitApiClientService.getSleep(user.getEmail(), logDate.toString());
                } catch (Exception e) {
                    log.error("Failed to fetch on-demand Fitbit data for user {}: {}", user.getId(), e.getMessage());
                }
            } else {
                log.info("Rate limit blocked the sync of the user's fitbit data syn for user {} on date {}", user.getPublicId(), logDate);
            }


            //Fetch fresh data from db:
            //Heart:
            heartLog = activitiesHeartLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

            //Activity:
            activitySummariesLog = activitySummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            activityGoalsLog = activityGoalsLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            activitiesLog = activityLogRepo.findByUserAndLogDate(user, logDate);

            //Sleep:
            sleepSummaryLog = sleepSummaryLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
            sleepLog = sleepLogRepo.findByUserAndLogDate(user, logDate).orElse(null);

            //log the data:
            log.info("Fresh heart log id={} for user={} on date={}", heartLog != null ? heartLog.getId() : null, user.getId(), logDate);
            log.info("Fresh activity summaries id={} for user={} on date={}", activitySummariesLog != null ? activitySummariesLog.getId() : null, user.getId(), logDate);
            log.info("Fresh sleep summary id={} for user={} on date={}", sleepSummaryLog != null ? sleepSummaryLog.getId() : null, user.getId(), logDate);


            //Extract resting heart rate if available or not null:
            if (heartLog != null && heartLog.getValues() != null && !heartLog.getValues().isEmpty()) {
                restingHeartRate = heartLog.getValues().get(0).getRestingHeartRate();
            } else {
                restingHeartRate = -1;
            }
        } else {
            log.info("User {} does not have fitbit connected. Skipping Fitbit data fetch for log date {}:", user.getId(), logDate);
        }

        //De-identifying the data before setting it up in the dto:
        String dayContext = deIdentificationHelper.sanitizeTheDate(logDate);
        String notes = deIdentificationHelper.sanitizeNotes(manualDailyLog.getNotes());
        HashMap<String, Integer> activityMap = helper.getActivityLogMap(activitiesLog);
        String humanReadableDescription = helper.getHumanReadableDescription(activityMap);

        return AiUserDailyInputDto.builder()
                .dayContext(dayContext)

                //User info:
                .gender(user.getGender())
                .age(user.getAge())
                .hasSurgeryHistory(user.getSurgeryHistory())
                .injuryTypes(injuryTypes)
                .surgeryTypes(surgeryTypes)
                .discLevels(discLevels)

                // Manual
                .painLevel(manualDailyLog.getPainLevel() != null ? EnumScoreHelper.pain(manualDailyLog.getPainLevel()) : -1)
                .flareUpToday(manualDailyLog.getFlareUpToday())
                .numbnessTingling(manualDailyLog.getNumbnessTingling())
                .sittingTime(manualDailyLog.getSittingTime() != null ? EnumScoreHelper.sittingTime(manualDailyLog.getSittingTime()) : -1)
                .standingTime(manualDailyLog.getStandingTime() != null ? EnumScoreHelper.standingTime(manualDailyLog.getStandingTime()) : -1)
                .stretchingDone(manualDailyLog.getStretchingDone())
                .morningStiffness(manualDailyLog.getMorningStiffness() != null ? EnumScoreHelper.morningStiffness(manualDailyLog.getMorningStiffness()) : -1)
                .stressLevel(manualDailyLog.getStressLevel() != null ? EnumScoreHelper.stressLevel(manualDailyLog.getStressLevel()) : -1)
                .liftingOrStrain(manualDailyLog.getLiftingOrStrain())
                .notes(notes)
                .sleepDuration(manualDailyLog.getSleepDuration() != null ? EnumScoreHelper.sleepDuration(manualDailyLog.getSleepDuration()) : -1)
                .nightWakeUps(manualDailyLog.getNightWakeUps() != null ? EnumScoreHelper.nightWakeUps(manualDailyLog.getNightWakeUps()) : -1)
                .manualRestingHeartRate(manualDailyLog.getRestingHeartRate() != null ? manualDailyLog.getRestingHeartRate() : -1)

                // Heart
                .fitbitRestingHeartRate(restingHeartRate)

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
                .description(humanReadableDescription)

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
