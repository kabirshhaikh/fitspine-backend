package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiInsightResponseDto {
    private String todaysInsight;
    private List<String> flareUpTriggers;
    private String recoveryInsights;
    private Integer discProtectionScore;
    private String discScoreExplanation;
}
