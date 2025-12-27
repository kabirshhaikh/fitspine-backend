package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlareUpActivitySummaryDto {
    private Integer flareUpDays;
    private Integer nonFlareUpDays;

    //Averages of flare up days:
    private Double flareAvgStanding;
    private Double flareAvgSitting;
    private Double flareAvgSedentaryHours;

    //Averages of non flare up days:
    private Double nonFlareAvgStanding;
    private Double nonFlareAvgSitting;
    private Double nonFlareAvgSedentaryHours;

    //Deltas:
    private Double standingDeltaPercent; //negative means worse on flare up days
    private Double sedentaryDeltaPercent; //positive means more sedentary on flare up days

    //Explanation, but not a list:
    private String summaryText;
}
