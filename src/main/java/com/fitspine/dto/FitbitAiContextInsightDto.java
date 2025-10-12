package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FitbitAiContextInsightDto {
    // Window metadata
    private Integer windowDays;
    private Integer daysAvailable;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime computedAt;

    //Manual log fields:
    private Integer averagePainLevel; //From ManualDailyLog
    private Integer averageSittingTime;
    private Integer averageStandingTime;
    private Integer averageMorningStiffness;
    private Integer averageStressLevel;
    private Integer daysWithStretching;
    private Integer daysWithFlareups;
    private Integer daysWithNumbnessTingling;
    private Integer daysWithLiftingOrStrain;

    //Fitbit Integration Data:
    //Heart
    private Integer averageRestingHeartRate; //From FitbitActivitiesHeartLog -> FitbitActivitiesHeartValueLog

    //Activities:
    private Integer averageCaloriesOut; //From FitbitActivitySummariesLog
    private Integer averageSteps;
    private Integer averageSedentaryMinutes;
    private Integer averageActiveMinutes; //From FitbitActivityGoalsLog

    //Sleep
    private Integer averageTotalMinutesAsleep; //From FitbitSleepSummaryLog
    private Integer averageEfficiency; //From FitbitSleepLog

    //New fields for risk forecast:
    private Integer yesterdaySleepMinutes;
    private Integer yesterdayRestingHeartRate;
    private Integer yesterdayPainLevel;
    private Integer daysSinceLastFlareUp;
    private Integer stepsStandardDeviation;
    private Integer restingHearRateStandardDeviation;
    private Integer sleepStandardDeviation;
    private Integer sedentaryStandardDeviation;
}