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
public class WeeklySummaryResultDto {
    private Double overAllSpineLoad; //Combined normalized spine load (0–4 scale)
    private String loadCategory; //low, moderate, high
    private String painTrend; //improving, worsening, stable
    private String activityBalance; //more sedentary / balanced / more active
    private String summaryText; //Full paragraph shown at top of Insights tab
    private List<String> improvements; //Positive changes detected this week
    private List<String> needsAttention; //Areas requiring attention
    private List<String> detectedPatterns; //Pattern detection insights

    private String nextWeekFocus;//Single most important focus for next week
}
