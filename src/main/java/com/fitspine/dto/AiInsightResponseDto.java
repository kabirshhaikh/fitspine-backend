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
public class AiInsightResponseDto {
    private String todaysInsight;
    private String recoveryInsights;
    private Integer discProtectionScore;
    private String discScoreExplanation;

    private List<FlareUpTriggersDto> flareUpTriggers;
    private List<String> worsened;
    private List<String> possibleCauses;
    private List<String> actionableAdvice;
    private List<String> interventionsToday;
    private RiskForecastDto riskForecast;
}
