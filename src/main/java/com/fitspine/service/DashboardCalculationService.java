package com.fitspine.service;

import com.fitspine.calculator.*;
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
    private final HeartCalculator heartCalculator;
    private final SleepCalculator sleepCalculator;

    public DashboardCalculationService(
            WeeklySummaryCalculator weeklySummaryCalculator,
            PainStiffnessCalculator painStiffnessCalculator,
            ActivityCalculator activityCalculator,
            HeartCalculator heartCalculator,
            SleepCalculator sleepCalculator
    ) {
        this.weeklySummaryCalculator = weeklySummaryCalculator;
        this.painStiffnessCalculator = painStiffnessCalculator;
        this.activityCalculator = activityCalculator;
        this.heartCalculator = heartCalculator;
        this.sleepCalculator = sleepCalculator;
    }

    public DashboardInsightDto calculate(WeeklyGraphDto weeklyGraph) {

        return DashboardInsightDto.builder()
                .weeklySummaryResultDto(weeklySummaryCalculator.calculate(weeklyGraph))
                .painStiffnessResultDto(painStiffnessCalculator.calculate(weeklyGraph))
                .activityResultDto(activityCalculator.calculate(weeklyGraph))
                .heartResultDto(heartCalculator.calculate(weeklyGraph))
                .sleepResultDto(sleepCalculator.calculate(weeklyGraph))
                .build();
    }
}


