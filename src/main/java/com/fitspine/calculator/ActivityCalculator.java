package com.fitspine.calculator;

import com.fitspine.dto.*;
import com.fitspine.helper.ActivityCalculatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ActivityCalculator {
    private final ActivityCalculatorHelper helper;

    public ActivityCalculator(
            ActivityCalculatorHelper helper
    ) {
        this.helper = helper;
    }

    public ActivityResultDto calculate(WeeklyGraphDto dto) {
        //Get all days of data:
        List<DailyGraphDto> allDays = dto.getDailyData();
        List<DailyGraphDto> loggedDays = helper.getLoggedDaysWithActivityData(allDays);

        if (loggedDays.isEmpty()) {
            return ActivityResultDto.builder().build();
        }

        //Activity balance:
        Double activityBalancePercent = helper.calculateActivityBalancePercent(loggedDays);

        Double standingGoalMetPercent = helper.calculateStandingGoalMetPercent(allDays);

        Double sedentaryLimitMetPercent = helper.calculateSedentaryLimitMetPercent(allDays);

        Double balanceGoalMetPercent = helper.calculateBalanceGoalMetPercent(allDays);

        DaySummaryDto bestStandingDay = helper.getBestStandingDay(allDays);

        DaySummaryDto worstStandingDay = helper.getWorstStandingDay(allDays);

        //If both bst and worst days are same then set them to null:
        if (bestStandingDay != null && worstStandingDay != null &&
                bestStandingDay.getValue().equals(worstStandingDay.getValue())
        ) {
            bestStandingDay = null;
            worstStandingDay = null;
        }

        List<ExplanationDto> explanations = helper.explainWhyActivityDecreased(bestStandingDay, worstStandingDay, allDays, dto.getIsFitbitConnected());

        return ActivityResultDto.builder()
                .activityBalancePercent(activityBalancePercent)
                .standingGoalMetPercent(standingGoalMetPercent)
                .sedentaryLimitMetPercent(sedentaryLimitMetPercent)
                .balanceGoalMetPercent(balanceGoalMetPercent)
                .bestStandingDay(bestStandingDay)
                .worstStandingDay(worstStandingDay)
                .explanations(explanations)
                .build();
    }
}
