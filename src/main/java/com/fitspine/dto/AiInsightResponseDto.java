package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiInsightResponseDto {
    private String todaysInsight;
    private String recoveryInsights;
    private Integer discProtectionScore;
    private String discScoreExplanation;

    private List<FlareUpTriggersDto> flareUpTriggers;
    private List<String> improved;
    private List<String> worsened;
    private List<String> possibleCauses;
    private List<String> actionableAdvice;
    private List<String> interventionsToday;
    private RiskForecastDto riskForecast;
}
