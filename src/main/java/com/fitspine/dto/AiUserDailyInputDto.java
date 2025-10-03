package com.fitspine.dto;

import com.fitspine.enums.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AiUserDailyInputDto {
    private Long id;
    private LocalDate logDate;

    //Manual log fields:
    private PainLevel painLevel; //From ManualDailyLog
    private Boolean flareUpToday;
    private Boolean numbnessTingling;
    private SittingTime sittingTime;
    private StandingTime standingTime;
    private Boolean stretchingDone;
    private MorningStiffness morningStiffness;
    private StressLevel stressLevel;
    private Boolean liftingOrStrain;
    private String notes;

    //Fitbit Integration Data:
    //Heart
    private Integer restingHeartRate; //From FitbitActivitiesHeartValueLog

    //Activities:
    private Integer caloriesOut; //From FitbitActivitySummariesLog
    private Integer activityCalories;
    private Integer caloriesBmr;
    private Integer activeScore;
    private Integer steps;
    private Integer sedentaryMinutes;
    private Integer lightlyActiveMinutes;
    private Integer fairlyActiveMinutes;
    private Integer veryActiveMinutes;
    private Integer marginalCalories;

    private Integer floors; //From FitbitActivityGoalsLog
    private Integer activeMinutes;

    private String description; //From FitbitActivitiesLog

    //Sleep
    private Integer totalMinutesAsleep; //From FitbitSleepSummaryLog
    private Integer totalSleepRecords;
    private Integer totalTimeInBed;

    private Integer efficiency; //From FitbitSleepLog
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer infoCode;
    private Boolean isMainSleep;
    private Integer minutesAfterWakeup;
    private Integer minutesAwake;
    private Integer minutesAsleep;
    private Integer minutesToFallAsleep;
    private String logType;
    private Integer timeInBed;
    private String type;
}
