package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitbitAiContextInsightDto {
    // Window metadata
    private Integer windowDays;
    private Integer daysAvailable;
    private String startDateContext;
    private String endDateContext;
    private String computedContext;

    //Manual log fields:
    private Integer averagePainLevel; //From ManualDailyLog
    private String averagePainLevelLabel;

    private Integer averageSittingTime;
    private String averageSittingTimeLabel;

    private Integer averageStandingTime;
    private String averageStandingTimeLabel;

    private Integer averageMorningStiffness;
    private String averageMorningStiffnessLabel;

    private Integer averageStressLevel;
    private String averageStressLevelLabel;

    private Integer daysWithStretching;
    private Integer daysWithFlareups;
    private Integer daysWithNumbnessTingling;
    private Integer daysWithLiftingOrStrain;
    private Integer averageSleepingDuration;
    private String averageSleepingDurationLabel;

    private Integer averageNightWakeUps;
    private String averageNightWakeUpsLabel;

    private Integer averageManualRestingHeartRate;

    //Fitbit Integration Data:
    //Heart
    private Integer averageFitbitRestingHeartRate; //From FitbitActivitiesHeartLog -> FitbitActivitiesHeartValueLog

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
    private Integer yesterdayFitbitRestingHeartRate;
    private Integer yesterdayPainLevel;
    private Integer daysSinceLastFlareUp;
    private Integer stepsStandardDeviation;
    private Integer restingHearRateStandardDeviation;
    private Integer sleepStandardDeviation;
    private Integer sedentaryStandardDeviation;
    private Integer yesterdayManualRestingHeartRate;
    private Integer yesterdaySleepDuration;
    private Integer yesterdayNightWakeUps;
}