package com.fitspine.calculator;

import com.fitspine.dto.*;
import com.fitspine.helper.PainStiffnessCalculatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        //i am doing this because if best pain day and worst pain day are equal then set them as null:
        if (bestPainDay != null && worstPainDay != null && bestPainDay.getValue().equals(worstPainDay.getValue())) {
            bestPainDay = null;
            worstPainDay = null;
        }

        //i am doing this because if best morning stiffness day and worst morning stiffness are equal then set them as null:
        if (bestMorningStiffnessDay != null && worstMorningStiffnessDay != null && bestMorningStiffnessDay.getValue().equals(worstMorningStiffnessDay.getValue())) {
            bestMorningStiffnessDay = null;
            worstMorningStiffnessDay = null;
        }

        //Now i calculate the co-relations:
        List<String> correlations = helper.detectPainCorrelation(loggedDays, dto.getIsFitbitConnected());

        List<String> userDiscIssues =
                Optional.ofNullable(dto.getUserDiscIssues())
                        .orElse(List.of());

        List<String> userInjuries =
                Optional.ofNullable(dto.getUserInjuryList())
                        .orElse(List.of());

        //Now i get the pain explanations:
        List<ExplanationDto> painExplanations = helper.explainPainChange(bestPainDay, worstPainDay, loggedDays, dto.getIsFitbitConnected(), userDiscIssues, userInjuries);

        //Now i get the stiffness explanations:
        List<ExplanationDto> stiffnessExplanations = helper.explainStiffnessChange(bestMorningStiffnessDay, worstMorningStiffnessDay, loggedDays, dto.getIsFitbitConnected(), userDiscIssues, userInjuries);

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
                .painExplanations(painExplanations)
                .stiffnessExplanation(stiffnessExplanations)
                .build();
    }
}
