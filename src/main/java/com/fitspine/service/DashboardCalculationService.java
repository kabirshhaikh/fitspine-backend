package com.fitspine.service;

import com.fitspine.calculator.ActivityCalculator;
import com.fitspine.calculator.PainStiffnessCalculator;
import com.fitspine.calculator.WeeklySummaryCalculator;
import com.fitspine.dto.DashboardInsightDto;
import com.fitspine.dto.WeeklyGraphDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DashboardCalculationService {
    private final WeeklySummaryCalculator weeklySummaryCalculator;
    private final PainStiffnessCalculator painStiffnessCalculator;
    private final ActivityCalculator activityCalculator;

    public DashboardCalculationService(
            WeeklySummaryCalculator weeklySummaryCalculator,
            PainStiffnessCalculator painStiffnessCalculator,
            ActivityCalculator activityCalculator
    ) {
        this.weeklySummaryCalculator = weeklySummaryCalculator;
        this.painStiffnessCalculator = painStiffnessCalculator;
        this.activityCalculator = activityCalculator;
    }

    public DashboardInsightDto calculate(WeeklyGraphDto weeklyGraph) {

        return DashboardInsightDto.builder()
                .weeklySummaryResultDto(weeklySummaryCalculator.calculate(weeklyGraph))
                .painStiffnessResultDto(painStiffnessCalculator.calculate(weeklyGraph))
                .activityResultDto(activityCalculator.calculate(weeklyGraph))
                .build();
    }
}


