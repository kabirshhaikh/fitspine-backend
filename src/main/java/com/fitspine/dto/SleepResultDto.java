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
public class SleepResultDto {
    private Double averageSleepHours;
    private TrendResultDto sleepTrend;

    private SleepQualityDto sleepQuality;
    private Double sleepConsistencyPercent;

    private DaySummaryDto bestSleepDay;
    private DaySummaryDto worstSleepDay;

    private List<ExplanationDto> explanations;
}
