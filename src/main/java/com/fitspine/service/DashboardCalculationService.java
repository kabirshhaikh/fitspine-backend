package com.fitspine.service;

import com.fitspine.calculator.WeeklySummaryCalculator;
import com.fitspine.dto.DashboardInsightDto;
import com.fitspine.dto.WeeklyGraphDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DashboardCalculationService {
    private final WeeklySummaryCalculator weeklySummaryCalculator;

    public DashboardCalculationService(
            WeeklySummaryCalculator weeklySummaryCalculator
    ) {
        this.weeklySummaryCalculator = weeklySummaryCalculator;
    }

    public DashboardInsightDto calculate(WeeklyGraphDto weeklyGraph) {

        return DashboardInsightDto.builder()
                .weeklySummaryResultDto(weeklySummaryCalculator.calculate(weeklyGraph))
                .build();
    }
}


