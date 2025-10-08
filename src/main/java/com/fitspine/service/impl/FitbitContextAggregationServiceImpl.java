package com.fitspine.service.impl;

import com.fitspine.dto.FitbitAiContextInsightDto;

import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.FitbitContextAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FitbitContextAggregationServiceImpl implements FitbitContextAggregationService {
    private final UserRepository userRepository;
    private final ManualDailyLogRepository manualDailyLogRepository;
    private final FitbitActivitiesHeartLogRepository heartLogRepository;
    private final FitbitActivitiesHeartValueLogRepository heartValueLogRepository;
    private final FitbitActivitySummariesLogRepository activitySummariesLogRepository;
    private final FitbitActivityGoalsLogRepository activityGoalsLogRepository;
    private final FitbitSleepLogRepository sleepLogRepository;
    private final FitbitSleepSummaryLogRepository sleepSummaryLogRepository;

    public FitbitContextAggregationServiceImpl(
            UserRepository userRepository,
            ManualDailyLogRepository manualDailyLogRepository,
            FitbitActivitiesHeartLogRepository heartLogRepository,
            FitbitActivitiesHeartValueLogRepository heartValueLogRepository,
            FitbitActivitySummariesLogRepository activitySummariesLogRepository,
            FitbitActivityGoalsLogRepository activityGoalsLogRepository,
            FitbitSleepLogRepository sleepLogRepository,
            FitbitSleepSummaryLogRepository sleepSummaryLogRepository
    ) {
        this.userRepository = userRepository;
        this.manualDailyLogRepository = manualDailyLogRepository;
        this.heartLogRepository = heartLogRepository;
        this.heartValueLogRepository = heartValueLogRepository;
        this.activitySummariesLogRepository = activitySummariesLogRepository;
        this.activityGoalsLogRepository = activityGoalsLogRepository;
        this.sleepLogRepository = sleepLogRepository;
        this.sleepSummaryLogRepository = sleepSummaryLogRepository;
    }


    @Override
    public FitbitAiContextInsightDto buildContext(String email, LocalDate targetDate) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email not found: " + email));
        LocalDate startDate = targetDate.minusDays(6);
        LocalDate endDate = targetDate.minusDays(1);

        List<ManualDailyLog> manualDailyLogs = manualDailyLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<FitbitActivitiesHeartLog> heartLogs = heartLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);
        List<Integer> restingHeartRateList = new ArrayList<>();

        for (int i = 0; i < heartLogs.size(); i++) {
            FitbitActivitiesHeartLog log = heartLogs.get(i);
            List<FitbitActivitiesHeartValueLog> valueLog = log.getValues();
            if (valueLog != null && !valueLog.isEmpty()) {
                for (int j = 0; j < valueLog.size(); j++) {
                    FitbitActivitiesHeartValueLog currentHeartValueLog = valueLog.get(j);
                    if (currentHeartValueLog.getRestingHeartRate() != null) {
                        restingHeartRateList.add(currentHeartValueLog.getRestingHeartRate());
                    }
                }
            }
        }

        List<FitbitActivitySummariesLog> activitySummariesLogs = activitySummariesLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);


        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog current = activitySummariesLogs.get(i);
            System.out.println("Calories Out:" + current.getCaloriesOut());
            System.out.println("Steps:" + current.getSteps());
            System.out.println("Sedentary Minutes:" + current.getSedentaryMinutes());
        }

        List<FitbitActivityGoalsLog> activityGoalsLogs = activityGoalsLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);

        for (int i = 0; i < activityGoalsLogs.size(); i++) {
            FitbitActivityGoalsLog current = activityGoalsLogs.get(i);
            System.out.println("Active minutes: " + current.getActiveMinutes());
        }

        List<FitbitSleepLog> sleepLogs = sleepLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);

        for (int i = 0; i < sleepLogs.size(); i++) {
            FitbitSleepLog current = sleepLogs.get(i);
            System.out.println("Efficiency: " + current.getEfficiency());
        }

        List<FitbitSleepSummaryLog> sleepSummaryLogs = sleepSummaryLogRepository.findByUserAndLogDateBetween(user, startDate, endDate);

        for (int i = 0; i < sleepSummaryLogs.size(); i++) {
            FitbitSleepSummaryLog current = sleepSummaryLogs.get(i);
            System.out.println("Total minutes asleep: " + current.getTotalMinutesAsleep());
        }

        return FitbitAiContextInsightDto.builder()
                // Metadata
                .windowDays(7)
                .daysAvailable(0) // computed dynamically based on available data
                .startDate(startDate)
                .endDate(endDate)
                .computedAt(LocalDateTime.now())

                // Manual Log Aggregates
                .averagePainLevel(0)
                .averageSittingTime(0)
                .averageStandingTime(0)
                .averageMorningStiffness(0)
                .averageStressLevel(0)
                .percentageDaysWithStretching(0)
                .percentageDaysWithFlareUp(0)
                .percentageDaysWithNumbnessTingling(0)
                .percentageDaysWithLiftingOrStrain(0)

                // Fitbit Heart Data
                .averageRestingHeartRate(0)

                // Fitbit Activity Data
                .averageCaloriesOut(0)
                .averageSteps(0)
                .averageSedentaryMinutes(0)
                .averageActiveMinutes(0)

                // Fitbit Sleep Data
                .averageTotalMinutesAsleep(0)
                .averageEfficiency(0)

                .build();
    }
}
