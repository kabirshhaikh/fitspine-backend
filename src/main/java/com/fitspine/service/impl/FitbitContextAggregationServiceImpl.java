package com.fitspine.service.impl;

import com.fitspine.dto.*;

import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.DeIdentificationHelper;
import com.fitspine.helper.FitbitContextAggregationHelper;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.FitbitContextAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Slf4j
@Service
public class FitbitContextAggregationServiceImpl implements FitbitContextAggregationService {
    private final UserRepository userRepository;
    private final ManualDailyLogRepository manualDailyLogRepository;
    private final FitbitActivitiesHeartLogRepository heartLogRepository;
    private final FitbitActivitySummariesLogRepository activitySummariesLogRepository;
    private final FitbitActivityGoalsLogRepository activityGoalsLogRepository;
    private final FitbitSleepLogRepository sleepLogRepository;
    private final FitbitSleepSummaryLogRepository sleepSummaryLogRepository;
    private final FitbitContextAggregationHelper helper;
    private final RedisTemplate<String, Object> redis;
    private final DeIdentificationHelper deIdentificationHelper;

    public FitbitContextAggregationServiceImpl(
            UserRepository userRepository,
            ManualDailyLogRepository manualDailyLogRepository,
            FitbitActivitiesHeartLogRepository heartLogRepository,
            FitbitActivitySummariesLogRepository activitySummariesLogRepository,
            FitbitActivityGoalsLogRepository activityGoalsLogRepository,
            FitbitSleepLogRepository sleepLogRepository,
            FitbitSleepSummaryLogRepository sleepSummaryLogRepository,
            FitbitContextAggregationHelper helper,
            RedisTemplate<String, Object> redis,
            DeIdentificationHelper deIdentificationHelper
    ) {
        this.userRepository = userRepository;
        this.manualDailyLogRepository = manualDailyLogRepository;
        this.heartLogRepository = heartLogRepository;
        this.activitySummariesLogRepository = activitySummariesLogRepository;
        this.activityGoalsLogRepository = activityGoalsLogRepository;
        this.sleepLogRepository = sleepLogRepository;
        this.sleepSummaryLogRepository = sleepSummaryLogRepository;
        this.helper = helper;
        this.redis = redis;
        this.deIdentificationHelper = deIdentificationHelper;
    }

    @Override
    public FitbitAiContextInsightDto buildContext(String email, LocalDate targetDate) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email not found: " + email));

        //Redis cache key:
        String cacheKey = "context:" + user.getPublicId() + ":" + targetDate;
        FitbitAiContextInsightDto cachedData = (FitbitAiContextInsightDto) redis.opsForValue().get(cacheKey);
        if (cachedData != null) {
            log.info("Returning cached context for user: {} on date: {}", user.getPublicId(), targetDate);
            return cachedData;
        }

        //If cached data is not present, then compute and store the data in redis at the end:
        LocalDate startDate = targetDate.minusDays(6);
        LocalDate endDate = targetDate.minusDays(1);

        //List of models between start and end date:
        List<ManualDailyLog> manualDailyLogs = manualDailyLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitActivitiesHeartLog> heartLogs = heartLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitActivitySummariesLog> activitySummariesLogs = activitySummariesLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitActivityGoalsLog> activityGoalsLogs = activityGoalsLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitSleepLog> sleepLogs = sleepLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitSleepSummaryLog> sleepSummaryLogs = sleepSummaryLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);

        //List of Metrics:
        List<Integer> restingHeartRates = helper.getRestingHeartRate(heartLogs);
        List<FitbitActivitySummariesMetricDto> activityMetrics = helper.getActivityMetric(activitySummariesLogs);
        List<FitbitActivityGoalsLogMetricDto> activityGoalsLogMetrics = helper.getGoalsMetrics(activityGoalsLogs);
        List<FitbitSleepLogMetricDto> sleepLogMetrics = helper.getSleepLogMetric(sleepLogs);
        List<FitbitSleepSummaryLogMetricDto> sleepSummaryMetrics = helper.getSleepSummaryMetrics(sleepSummaryLogs);

        //De-Identified data:
        String computedContext = deIdentificationHelper.sanitizeTheDate(LocalDate.now());
        String startDateContext = deIdentificationHelper.sanitizeTheDateForContextBuilding(startDate, "start");
        String endDateContext = deIdentificationHelper.sanitizeTheDateForContextBuilding(endDate, "end");

        FitbitAiContextInsightDto dto = FitbitAiContextInsightDto.builder()
                // Metadata
                .windowDays(7)
                .daysAvailable(helper.calculateDaysAvailable(manualDailyLogs, heartLogs, activitySummariesLogs, activityGoalsLogs, sleepLogs, sleepSummaryLogs))
                .startDateContext(startDateContext)
                .endDateContext(endDateContext)
                .computedContext(computedContext)

                // Manual Log Aggregates
                .averagePainLevel(helper.calculateAveragePainLevel(manualDailyLogs))
                .averageSittingTime(helper.calculateAverageSittingTime(manualDailyLogs))
                .averageStandingTime(helper.calculateAverageStandingTime(manualDailyLogs))
                .averageMorningStiffness(helper.calculateAverageMorningStiffness(manualDailyLogs))
                .averageStressLevel(helper.calculateAverageStressLevel(manualDailyLogs))
                .daysWithStretching(helper.calculateDaysWithStretching(manualDailyLogs))
                .daysWithFlareups(helper.calculateDaysWithFlareUp(manualDailyLogs))
                .daysWithNumbnessTingling(helper.calculateDaysWithNumbnessTingling(manualDailyLogs))
                .daysWithLiftingOrStrain(helper.calculateDaysWithLiftingOrStrain(manualDailyLogs))
                .averageSleepingDuration(helper.calculateAverageSleepingDuration(manualDailyLogs))
                .averageNightWakeUps(helper.calculateAverageNightWakeUps(manualDailyLogs))
                .averageManualRestingHeartRate(helper.calculateAverageManualRestingHeartRate(manualDailyLogs))

                // Fitbit Heart Data
                .averageFitbitRestingHeartRate(helper.calculateAverageRestingHeartRate(restingHeartRates))

                // Fitbit Activity Data
                .averageCaloriesOut(helper.calculateAverageCaloriesOut(activityMetrics))
                .averageSteps(helper.calculateAverageSteps(activityMetrics))
                .averageSedentaryMinutes(helper.calculateAverageSedentaryMinutes(activityMetrics))
                .averageActiveMinutes(helper.calculateAverageActiveMinutes(activityGoalsLogMetrics))

                // Fitbit Sleep Data
                .averageTotalMinutesAsleep(helper.calculateAverageTotalMinutesAsleep(sleepSummaryMetrics))
                .averageEfficiency(helper.calculateAverageEfficiency(sleepLogMetrics))

                //Risk forecast:
                .yesterdaySleepMinutes(helper.getYesterdaysSleep(sleepSummaryLogs))
                .yesterdayFitbitRestingHeartRate(helper.getYesterdaysRestingHeartRate(heartLogs))
                .yesterdayPainLevel(helper.getYesterdaysPainLevel(manualDailyLogs))
                .daysSinceLastFlareUp(helper.calculateDaysSinceLastFlareUp(manualDailyLogs))
                .yesterdayManualRestingHeartRate(helper.getYesterdaysManualRestingHeartRate(manualDailyLogs))
                .yesterdaySleepDuration(helper.getYesterdaysSleepDuration(manualDailyLogs))
                .yesterdayNightWakeUps(helper.getYesterdaysNightWakeUps(manualDailyLogs))

                //Standard Deviations:
                .stepsStandardDeviation(helper.calculateStepsStandardDeviation(activitySummariesLogs))
                .restingHearRateStandardDeviation(helper.calculateRestingHeartRateStandardDeviation(heartLogs))
                .sleepStandardDeviation(helper.calculateSleepStandardDeviation(sleepSummaryLogs))
                .sedentaryStandardDeviation(helper.calculateSedentaryStandardDeviation(activitySummariesLogs))

                .build();

        //Cache the data:
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        //Calculate the start of next day in UTC: 12 am basically:
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC);

        //Time to live:
        Duration timeToLive = Duration.between(now, midnight);

        //set the cache with key, dto and ttl:
        redis.opsForValue().set(cacheKey, dto, timeToLive);
        log.info("Cached context aggregation for user {} on date {}", user.getPublicId(), targetDate);

        return dto;
    }

    @Override
    public WeeklyGraphDto generateWeeklyGraph(LocalDate targetDate, User user) {
        LocalDate startDate = targetDate.minusDays(7);
        LocalDate endDate = targetDate.minusDays(1);

        List<String> discLevels =
                Optional.ofNullable(user.getUserDiscIssueList())
                        .orElse(List.of())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(UserDiscIssue::getDiscLevel)
                        .filter(Objects::nonNull)
                        .map(Enum::name)
                        .toList();


        List<String> injuries =
                Optional.ofNullable(user.getUserInjuryList())
                        .orElse(List.of())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(UserInjury::getInjuryType)
                        .filter(Objects::nonNull)
                        .map(Enum::name)
                        .toList();


        log.info("Target date for weekly graph: {}", targetDate);
        log.info("Start date for weekly graph: {}", startDate);
        log.info("End date for weekly graph: {}", endDate);

        //Redis key:
        String key = "weekly_graph:" + user.getPublicId() + ":" + targetDate;
        WeeklyGraphDto cachedResponse = (WeeklyGraphDto) redis.opsForValue().get(key);

        if (cachedResponse != null) {
            log.info("Returned cached response for weekly graph {}", key);
            return cachedResponse;
        }

        log.info("Missing Cache value for weekly graph {}", key);

        //Extract data between start and end date:
        List<ManualDailyLog> manualDailyLogs = manualDailyLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitActivitiesHeartLog> heartLogs = heartLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitActivitySummariesLog> activitySummariesLogs = activitySummariesLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitSleepSummaryLog> sleepSummaryLogs = sleepSummaryLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);

        //Extract list of metrics using helper class:
        //Manual:
        Map<LocalDate, Integer> painLevels = helper.getPainLevels(manualDailyLogs);
        Map<LocalDate, Integer> morningStiffness = helper.getMorningStiffness(manualDailyLogs);
        Map<LocalDate, Integer> sittingTime = helper.getSittingTime(manualDailyLogs);
        Map<LocalDate, Integer> standingTime = helper.getStandingTime(manualDailyLogs);
        Map<LocalDate, Integer> stressLevel = helper.getStressLevel(manualDailyLogs);
        Map<LocalDate, Integer> sleepDuration = helper.getSleepDuration(manualDailyLogs);
        Map<LocalDate, Integer> nightWakeUps = helper.getNightWakeUps(manualDailyLogs);
        Map<LocalDate, Integer> manualRestingHeart = helper.getManualRestingHeartRate(manualDailyLogs);

        //Fitbit:
        Map<LocalDate, Integer> fitbitRestingHeartRate = helper.getRestingHeartRateForWeeklyGraph(heartLogs);
        Map<LocalDate, Double> fitbitSedentaryHours = helper.getSedentaryHours(activitySummariesLogs);
        Map<LocalDate, Integer> fitbitTotalMinutesAsleep = helper.getTotalMinutesAsleep(sleepSummaryLogs);


        List<DailyGraphDto> dailyData = helper.getDailyDataBetweenDates(
                startDate,
                endDate,
                fitbitRestingHeartRate,
                painLevels,
                morningStiffness,
                sittingTime,
                standingTime,
                stressLevel,
                fitbitSedentaryHours,
                sleepDuration,
                nightWakeUps,
                manualRestingHeart,
                fitbitTotalMinutesAsleep
        );

        WeeklyGraphDto dto = WeeklyGraphDto.builder()
                .isFitbitConnected(user.getIsWearableConnected())
                .dailyData(dailyData)
                .userDiscIssues(discLevels)
                .userInjuryList(injuries)
                .build();

        if (cachedResponse == null) {
            log.info("Writing cache into redis for weekly graph..");
            redis.opsForValue().set(key, dto, Duration.ofHours(12));
        }

        return dto;
    }
}
