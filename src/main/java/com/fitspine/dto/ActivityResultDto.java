package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResultDto {
    //Activity balance:
    private Double activityBalancePercent;

    //Goal tracking:
    private Double standingGoalMetPercent; //% of days >= standing time enum 3
    private Double sedentaryLimitMetPercent; //% of days < 11 sedentary hours
    private Double balanceGoalMetPercent; // % of days >= 60% standing

    //Best/worst days:
    private DaySummaryDto bestStandingDay;
    private DaySummaryDto worstStandingDay;

    //Explanations:
    private List<ExplanationDto> explanations;

    //Activity related flare ups:
    private FlareUpActivitySummaryDto flareUpDto;

    List<ActivityBreakDownDto> dailyBreakDown;

    private Double standingAvg;
    private Double sittingAvg;
    private Double sedentaryAvg;

    private TrendResultDto standingTrend;
    private TrendResultDto sittingTrend;
    private TrendResultDto sedentaryTrend;

    private List<String> painCorrelations;
}
