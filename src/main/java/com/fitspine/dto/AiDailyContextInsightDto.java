package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AiDailyContextInsightDto {
    private Long id;
    private LocalDate logDate;

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
    private Integer percentageDaysWithStretching;
    private Integer percentageDaysWithFlareUp;
    private Integer percentageDaysWithNumbnessTingling;
    private Integer percentageDaysWithLiftingOrStrain;

    //Fitbit Integration Data:
    //Heart
    private Integer averageRestingHeartRate; //From FitbitActivitiesHeartValueLog

    //Activities:
    private Integer averageCaloriesOut; //From FitbitActivitySummariesLog
    private Integer averageSteps;
    private Integer averageSedentaryMinutes;
    private Integer averageActiveMinutes; //From FitbitActivityGoalsLog

    //Sleep
    private Integer averageTotalMinutesAsleep; //From FitbitSleepSummaryLog
    private Integer averageEfficiency; //From FitbitSleepLog
}
