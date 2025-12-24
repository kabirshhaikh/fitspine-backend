package com.fitspine.calculator;

import com.fitspine.dto.*;
import com.fitspine.helper.HeartCalculatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HeartCalculator {
    private final HeartCalculatorHelper helper;

    public HeartCalculator(HeartCalculatorHelper helper) {
        this.helper = helper;
    }

    public HeartResultDto calculate(WeeklyGraphDto dto) {
        // TODO:
        //Extract daily data:
        List<DailyGraphDto> allDays = dto.getDailyData();
        boolean isFitbitConnected = dto.getIsFitbitConnected();

        //Determine isFitbitConnected, fitbit is preferred else fallback to manual:
        List<DailyGraphDto> validHeartRateDays = helper.getValidHeartRateDays(allDays, isFitbitConnected);

        if (validHeartRateDays.isEmpty()) {
            return HeartResultDto.builder().build();
        }

        //at this point validHeartRateDays contains only days with a resolvable HR value:
        List<Integer> heartRateValues = helper.getHeartRateValues(validHeartRateDays, isFitbitConnected);

        //compute average HR:
        Double averageRestingHeartRate = helper.calculateAverageRestingHeartRate(heartRateValues);


        //Compute trend
        TrendResultDto restingHeartRateTrend = helper.calculateTrend(heartRateValues, "restingHeartRate");


        //Compute recovery change
        Integer recoveryChange = null;

        if (validHeartRateDays.size() >= 2) {
            DailyGraphDto firstDay = validHeartRateDays.get(0);
            DailyGraphDto lastDay = validHeartRateDays.get(validHeartRateDays.size() - 1);

            Integer firstHR = helper.getHeartRateValue(firstDay, isFitbitConnected);
            Integer lastHR = helper.getHeartRateValue(lastDay, isFitbitConnected);

            if (firstHR != null && lastHR != null) {
                int diff = firstHR - lastHR;

                //threshold: > 2bpm:
                if (Math.abs(diff) > 2) {
                    recoveryChange = diff;
                }
            }
        }

        //Compute consistency
        Double recoveryConsistencyPercent = helper.calculateRecoveryConsistencyPercent(heartRateValues, averageRestingHeartRate);

        //Detect stress correlation
        List<String> stressCorrelationInsights = helper.detectStressHeartRateCorrelation(validHeartRateDays, isFitbitConnected);

        //Determine best/worst days
        DaySummaryDto bestHeartRateDay = helper.getBestHeartRateDay(validHeartRateDays, isFitbitConnected);
        DaySummaryDto worstHeartRateDay = helper.getWorstHeartRateDay(validHeartRateDays, isFitbitConnected);

        if (bestHeartRateDay != null && worstHeartRateDay != null && bestHeartRateDay.getValue().equals(worstHeartRateDay.getValue())) {
            bestHeartRateDay = null;
            worstHeartRateDay = null;
        }

        // 9. Build explanations
        DailyGraphDto bestDay = null;
        DailyGraphDto worstDay = null;

        for (DailyGraphDto day : validHeartRateDays) {
            if (bestHeartRateDay != null &&
                    day.getDate().equals(bestHeartRateDay.getDate())) {
                bestDay = day;
            }

            if (worstHeartRateDay != null &&
                    day.getDate().equals(worstHeartRateDay.getDate())) {
                worstDay = day;
            }
        }

        List<ExplanationDto> explanations = helper.explainWhyHeartRateChanged(bestDay, worstDay, isFitbitConnected);

        return HeartResultDto.builder()
                .averageRestingHeartRate(averageRestingHeartRate)
                .restingHeartRateTrend(restingHeartRateTrend)
                .recoveryChange(recoveryChange)
                .recoveryConsistencyPercent(recoveryConsistencyPercent)
                .stressCorrelationInsights(stressCorrelationInsights)
                .bestHeartRateDay(bestHeartRateDay)
                .worstHeartRateDay(worstHeartRateDay)
                .explanations(explanations)
                .build();
    }
}
