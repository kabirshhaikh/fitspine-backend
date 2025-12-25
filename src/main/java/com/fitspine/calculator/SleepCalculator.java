package com.fitspine.calculator;

import com.fitspine.dto.*;
import com.fitspine.helper.SleepCalculatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SleepCalculator {
    private final SleepCalculatorHelper helper;

    public SleepCalculator(SleepCalculatorHelper helper) {
        this.helper = helper;
    }

    public SleepResultDto calculate(WeeklyGraphDto dto) {
        List<DailyGraphDto> allDays = dto.getDailyData();
        boolean isFitbitConnected = dto.getIsFitbitConnected();

        //Get valid sleep days:
        List<DailyGraphDto> validSleepDays = helper.getValidSleepDays(allDays, isFitbitConnected);
        if (validSleepDays.isEmpty()) {
            return SleepResultDto.builder().build();
        }

        //Convert sleep to hours:
        List<Double> sleepHoursValues = helper.getSleepHoursValues(validSleepDays, isFitbitConnected);
        if (sleepHoursValues.isEmpty()) {
            return SleepResultDto.builder().build();
        }

        //Average:
        Double averageSleepHours = helper.calculateAverageSleepHours(sleepHoursValues);

        //Trend:
        TrendResultDto sleepTrend = helper.calculateSleepTrend(sleepHoursValues);

        //Sleep Quality:
        SleepQualityDto sleepQuality = helper.getSleepQualityLabel(averageSleepHours);

        //Sleep consistency:
        Double sleepConsistencyPercent = helper.calculateSleepConsistency(sleepHoursValues, averageSleepHours);

        //Best day:
        DaySummaryDto bestSleepDay = helper.getBestDay(allDays, isFitbitConnected);

        //Worst day:
        DaySummaryDto worstSleepDay = helper.getWorstSleepDay(allDays, isFitbitConnected);

        if (bestSleepDay != null && worstSleepDay != null && bestSleepDay.getValue().equals(worstSleepDay.getValue())) {
            bestSleepDay = null;
            worstSleepDay = null;
        }

        //Explanations:
        List<ExplanationDto> explanations = helper.explainWhySleepChanged(
                bestSleepDay != null ? helper.findDayByDate(allDays, bestSleepDay.getDate()) : null,
                worstSleepDay != null ? helper.findDayByDate(allDays, worstSleepDay.getDate()) : null
        );

        return SleepResultDto.builder()
                .averageSleepHours(averageSleepHours)
                .sleepTrend(sleepTrend)
                .sleepQuality(sleepQuality)
                .sleepConsistencyPercent(sleepConsistencyPercent)
                .bestSleepDay(bestSleepDay)
                .worstSleepDay(worstSleepDay)
                .explanations(explanations)
                .build();
    }
}
