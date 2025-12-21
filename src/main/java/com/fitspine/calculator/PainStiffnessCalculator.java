package com.fitspine.calculator;

import com.fitspine.dto.*;
import com.fitspine.helper.PainStiffnessCalculatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PainStiffnessCalculator {
    private final PainStiffnessCalculatorHelper helper;
    public static final boolean LOWER_IS_BETTER = true;

    public PainStiffnessCalculator(
            PainStiffnessCalculatorHelper helper
    ) {
        this.helper = helper;
    }

    public PainStiffnessResultDto calculate(WeeklyGraphDto dto) {
        //Get logged days
        List<DailyGraphDto> allDays = dto.getDailyData();
        List<DailyGraphDto> loggedDays = helper.getLoggedDays(allDays);

        if (loggedDays.isEmpty()) {
            return PainStiffnessResultDto.builder().build();
        }

        //1st: I calculate the averages:
        //Here i initialize empty list to gather pain and morning stiffness levels:
        List<Integer> painValueList = new ArrayList<>();
        List<Integer> morningStiffnessValueList = new ArrayList<>();

        //Here i loop over the logged days array and add values into the list:
        for (int i = 0; i < loggedDays.size(); i++) {
            DailyGraphDto current = loggedDays.get(i);
            painValueList.add(current.getPainLevel());
            morningStiffnessValueList.add(current.getMorningStiffness());
        }

        //Pain Average:
        Double painAverage = helper.calculateAverage(painValueList);

        //Morning stiffness Average:
        Double morningStiffnessAverage = helper.calculateAverage(morningStiffnessValueList);

        //2nd i calculate the trend:
        TrendResultDto painTrend = helper.calculateTrendFromValues(painValueList, LOWER_IS_BETTER);
        TrendResultDto morningStiffnessTrend = helper.calculateTrendFromValues(morningStiffnessValueList, LOWER_IS_BETTER);

        //Now i track best and worst day for pain and morning stiffness:
        DaySummaryDto bestPainDay = helper.getBestDay(loggedDays, DailyGraphDto::getPainLevel);
        DaySummaryDto worstPainDay = helper.getWorstDay(loggedDays, DailyGraphDto::getPainLevel);

        DaySummaryDto bestMorningStiffnessDay = helper.getBestDay(loggedDays, DailyGraphDto::getMorningStiffness);
        DaySummaryDto worstMorningStiffnessDay = helper.getWorstDay(loggedDays, DailyGraphDto::getMorningStiffness);

        //Now i calculate the co-relations:
        List<String> correlations = helper.detectPainCorrelation(loggedDays, dto.getIsFitbitConnected());

        //Now i get the explanations:
        List<ExplanationDto> explanations = helper.explainPainChange(bestPainDay, worstPainDay, loggedDays, dto.getIsFitbitConnected());

        return PainStiffnessResultDto.builder()
                .painAverage(painAverage)
                .stiffnessAverage(morningStiffnessAverage)
                .painTrend(painTrend)
                .stiffnessTrend(morningStiffnessTrend)
                .bestPainDay(bestPainDay)
                .worstPainDay(worstPainDay)
                .bestStiffnessDay(bestMorningStiffnessDay)
                .worstStiffnessDay(worstMorningStiffnessDay)
                .correlations(correlations)
                .explanations(explanations)
                .build();
    }
}
