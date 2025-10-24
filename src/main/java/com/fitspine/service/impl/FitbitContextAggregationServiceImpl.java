package com.fitspine.service.impl;

import com.fitspine.dto.*;

import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.FitbitContextAggregationHelper;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.FitbitContextAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    public FitbitContextAggregationServiceImpl(
            UserRepository userRepository,
            ManualDailyLogRepository manualDailyLogRepository,
            FitbitActivitiesHeartLogRepository heartLogRepository,
            FitbitActivitySummariesLogRepository activitySummariesLogRepository,
            FitbitActivityGoalsLogRepository activityGoalsLogRepository,
            FitbitSleepLogRepository sleepLogRepository,
            FitbitSleepSummaryLogRepository sleepSummaryLogRepository,
            FitbitContextAggregationHelper helper,
            RedisTemplate<String, Object> redis
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
    }


    @Override
    public FitbitAiContextInsightDto buildContext(String email, LocalDate targetDate) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email not found: " + email));
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

        return FitbitAiContextInsightDto.builder()
                // Metadata
                .windowDays(7)
                .daysAvailable(helper.calculateDaysAvailable(manualDailyLogs, heartLogs, activitySummariesLogs, activityGoalsLogs, sleepLogs, sleepSummaryLogs))
                .startDate(startDate)
                .endDate(endDate)
                .computedAt(LocalDateTime.now())

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

                // Fitbit Heart Data
                .averageRestingHeartRate(helper.calculateAverageRestingHeartRate(restingHeartRates))

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
                .yesterdayRestingHeartRate(helper.getYesterdaysRestingHeartRate(heartLogs))
                .yesterdayPainLevel(helper.getYesterdaysPainLevel(manualDailyLogs))
                .daysSinceLastFlareUp(helper.calculateDaysSinceLastFlareUp(manualDailyLogs))

                //Standard Deviations:
                .stepsStandardDeviation(helper.calculateStepsStandardDeviation(activitySummariesLogs))
                .restingHearRateStandardDeviation(helper.calculateRestingHeartRateStandardDeviation(heartLogs))
                .sleepStandardDeviation(helper.calculateSleepStandardDeviation(sleepSummaryLogs))
                .sedentaryStandardDeviation(helper.calculateSedentaryStandardDeviation(activitySummariesLogs))

                .build();
    }

    @Override
    public WeeklyGraphDto generateWeeklyGraph(LocalDate targetDate, User user) {
        LocalDate startDate = targetDate.minusDays(7);
        LocalDate endDate = targetDate.minusDays(1);

        //Redis key:
        String key = "weekly_graph:" + user.getEmail() + ":" + targetDate;
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

        //Extract list of metrics using helper class:
        List<Integer> restingHeartRate = helper.getRestingHeartRateForWeeklyGraph(heartLogs);
        List<Integer> painLevels = helper.getPainLevels(manualDailyLogs);
        List<Integer> morningStiffness = helper.getMorningStiffness(manualDailyLogs);
        List<Integer> sittingTime = helper.getSittingTime(manualDailyLogs);
        List<Integer> standingTime = helper.getStandingTime(manualDailyLogs);
        List<Integer> stressLevel = helper.getStressLevel(manualDailyLogs);
        List<Double> sedentaryHours = helper.getSedentaryHours(activitySummariesLogs);
        List<String> dates = helper.getDatesForWeeklyGraph(startDate);

        WeeklyGraphDto dto = WeeklyGraphDto.builder()
                .dates(dates)
                .restingHeartRate(restingHeartRate)
                .painLevel(painLevels)
                .morningStiffness(morningStiffness)
                .sittingTime(sittingTime)
                .standingTime(standingTime)
                .stressLevel(stressLevel)
                .sedentaryHours(sedentaryHours)
                .build();

        if (cachedResponse == null) {
            log.info("Writing cache into redis for weekly graph..");
            redis.opsForValue().set(key, dto, Duration.ofHours(12));
        }

        return dto;
    }
}
