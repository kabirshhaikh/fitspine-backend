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

        //Now i calculate the flare up days:
        List<DailyGraphDto> flareUpDays = helper.getFlareUpDays(loggedDays);

        //Now i get flareup pain days:
        List<DailyGraphDto> flareUpPainDays = helper.filterDaysWithValidMetric(flareUpDays, DailyGraphDto::getPainLevel);

        //Now i get best pain flareup day and worst pain flareup day:
        DaySummaryDto bestPainFlareUpDay = helper.getBestDay(flareUpPainDays, DailyGraphDto::getPainLevel);
        DaySummaryDto worstPainFlareUpDay = helper.getWorstDay(flareUpPainDays, DailyGraphDto::getPainLevel);

        if (bestPainFlareUpDay != null && worstPainFlareUpDay != null && bestPainFlareUpDay.getValue().equals(worstPainFlareUpDay.getValue())) {
            bestPainFlareUpDay = null;
            worstPainFlareUpDay = null;
        }

        //Now i get flareup stiffness days:
        List<DailyGraphDto> flareUpStiffnessDays = helper.filterDaysWithValidMetric(flareUpDays, DailyGraphDto::getMorningStiffness);

        //Now i get best stiffness flareup day and worst stiffness flareup day:
        DaySummaryDto bestStiffnessFlareUpDay = helper.getBestDay(flareUpStiffnessDays, DailyGraphDto::getMorningStiffness);
        DaySummaryDto worstStiffnessFlareUpDay = helper.getWorstDay(flareUpStiffnessDays, DailyGraphDto::getMorningStiffness);

        if (bestStiffnessFlareUpDay != null && worstStiffnessFlareUpDay != null && bestStiffnessFlareUpDay.getValue().equals(worstStiffnessFlareUpDay.getValue())) {
            bestStiffnessFlareUpDay = null;
            worstStiffnessFlareUpDay = null;
        }


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
                .bestPainFlareUpDay(bestPainFlareUpDay)
                .worstPainFlareUpDay(worstPainFlareUpDay)
                .bestStiffnessFlareUpDay(bestStiffnessFlareUpDay)
                .worstStiffnessFlareUpDay(worstStiffnessFlareUpDay)
                .build();
    }
}
