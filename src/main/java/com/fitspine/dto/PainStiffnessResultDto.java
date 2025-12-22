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
public class PainStiffnessResultDto {
    private Double painAverage;
    private Double stiffnessAverage;

    private TrendResultDto painTrend;
    private TrendResultDto stiffnessTrend;

    private DaySummaryDto bestPainDay;
    private DaySummaryDto worstPainDay;

    private DaySummaryDto bestStiffnessDay;
    private DaySummaryDto worstStiffnessDay;

    private List<String> correlations;
    private List<ExplanationDto> painExplanations;
    private List<ExplanationDto> stiffnessExplanation;
}
