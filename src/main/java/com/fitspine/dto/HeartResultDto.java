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
public class HeartResultDto {
    private Double averageRestingHeartRate; //weekly average resting heart rate. Fitbit is preferred else fallback to normal

    private TrendResultDto restingHeartRateTrend; //weekly trend for resting heart rate

    private Integer recoveryChange; //recovery improvement over the week

    private Double recoveryConsistencyPercent; //heart rate consistency. (1 - (stdDev / avg)) * 100

    private List<String> stressCorrelationInsights; // stress <-> heart rate correlation insights

    private DaySummaryDto bestHeartRateDay;

    private DaySummaryDto worstHeartRateDay;

    private List<ExplanationDto> explanations;

    private List<HeartBreakDownDto> dailyBreakDown;
}
