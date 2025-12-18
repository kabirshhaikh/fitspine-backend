package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyGraphDto {
    private String date;

    //Manual:
    private Integer painLevel;
    private Integer sittingTime;
    private Integer standingTime;
    private Integer morningStiffness;
    private Integer stressLevel;
    private Integer sleepDuration;
    private Integer nightWakeUps;
    private Integer manualRestingHeartRate;

    //Fitbit:
    private Integer fitbitTotalMinutesAsleep;
    private Double fitbitSedentaryHours;
    private Integer fitbitRestingHeartRate;
}
