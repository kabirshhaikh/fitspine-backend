package com.fitspine.calculator;

import com.fitspine.dto.DailyGraphDto;
import com.fitspine.dto.WeeklyGraphDto;
import com.fitspine.dto.WeeklySummaryResultDto;
import com.fitspine.helper.WeeklySummaryCalculatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WeeklySummaryCalculator {
    private static final int EXPECTED_DAYS = 7;

    private final WeeklySummaryCalculatorHelper helper;

    public WeeklySummaryCalculator(WeeklySummaryCalculatorHelper helper) {
        this.helper = helper;
    }

    public WeeklySummaryResultDto calculate(WeeklyGraphDto dto) {
        //Get logged days
        List<DailyGraphDto> allDays = dto.getDailyData();
        List<DailyGraphDto> loggedDays = helper.getLoggedDays(allDays);

        //Get first and last day:
        DailyGraphDto firstDay = loggedDays.isEmpty() ? null : loggedDays.get(0);
        DailyGraphDto lastDay = loggedDays.isEmpty() ? null : loggedDays.get(loggedDays.size() - 1);

        //Missing days:
        int missingDays = EXPECTED_DAYS - loggedDays.size();

        //New user detection
        boolean isNewUser = loggedDays.isEmpty() || ((double) missingDays / EXPECTED_DAYS) >= 0.7;

        //Now i build list of painValues, stiffness, sitting, standing, stress and sedentary which is from fitbit:
        List<Integer> painValues = new ArrayList<>();
        List<Integer> morningStiffnessValues = new ArrayList<>();
        List<Integer> sittingValues = new ArrayList<>();
        List<Integer> standingValues = new ArrayList<>();
        List<Integer> stressValues = new ArrayList<>();
        List<Double> sedentaryValues = new ArrayList<>();

        //Now here i extract all the fields from logged days and add it into respective list's:
        for (int i = 0; i < loggedDays.size(); i++) {
            DailyGraphDto current = loggedDays.get(i);
            painValues.add(current.getPainLevel());
            morningStiffnessValues.add(current.getMorningStiffness());
            sittingValues.add(current.getSittingTime());
            standingValues.add(current.getStandingTime());
            stressValues.add(current.getStressLevel());
            sedentaryValues.add(current.getFitbitSedentaryHours());
        }

        //Now i get the averages of the fields for which i created the list:
        Double painLevel = helper.calculateAverage(painValues);
        Double morningStiffness = helper.calculateAverage(morningStiffnessValues);
        Double sittingTime = helper.calculateAverage(sittingValues);
        Double standingTime = helper.calculateAverage(standingValues);
        Double stressLevel = helper.calculateAverage(stressValues);
        Double fitbitSedentary = helper.calculateDoubleAverage(sedentaryValues);

        //Now i add components to an array called spine load components which i will use later to calculate overall spine load:
        List<Double> spineLoadComponents = helper.addComponentsToSpineOverLoadArray(painLevel, morningStiffness, sittingTime, fitbitSedentary);

        //Now i run a for loop on the spineLoadComponents and take the average:
        Double overAllSpineLoad = null;
        if (!spineLoadComponents.isEmpty()) {
            overAllSpineLoad = helper.calculateOverallSpineLoad(spineLoadComponents);
        }

        String loadCategory = null;
        if (overAllSpineLoad != null) {
            loadCategory = helper.returnLoadCategoryString(overAllSpineLoad);
        }


        //Now i calculate pain trend:
        String painTrend = "stable";

        if (firstDay != null && lastDay != null) {
            Integer firstPain = firstDay.getPainLevel();
            Integer lastPain = lastDay.getPainLevel();

            if (helper.isValid(firstPain) && helper.isValid(lastPain)) {
                if (lastPain < firstPain) {
                    painTrend = "improving";
                } else if (lastPain > firstPain) {
                    painTrend = "worsening";
                }
            }
        }

        //Now i calculate activity balance:
        String activityBalance = null;

        if (sittingTime != null && standingTime != null) {
            if (sittingTime > standingTime + 1) {
                activityBalance = "more sedentary";
            } else if (standingTime > sittingTime + 1) {
                activityBalance = "more active";
            } else {
                activityBalance = "balanced";
            }
        }

        //Now i create weekly summary text:
        StringBuilder summaryBuilder = new StringBuilder();

        if (loadCategory != null) {
            summaryBuilder.append("This week shows ")
                    .append(loadCategory)
                    .append(" overall spine load. ");
        }

        if (painLevel != null) {
            summaryBuilder.append("Pain levels are ")
                    .append(painTrend)
                    .append(" compared to the start of the week. ");
        }

        if (activityBalance != null) {
            summaryBuilder.append("Activity patterns show a ")
                    .append(activityBalance)
                    .append(" balance between sitting and standing time.");
        }

        String summaryText = summaryBuilder.toString().trim();

        //List of needs attentions improvements this week:
        List<String> improvements = helper.getListOfImprovements(firstDay, lastDay);

        //List of needs attentions this week:
        List<String> needsAttention = helper.getListOfNeedsAttention(fitbitSedentary, firstDay, lastDay, allDays.size(), loggedDays.size()
                , painLevel, stressLevel, isNewUser);

        //Now i build next weeks focus:
        String nextWeekFocus = helper.getNextWeeksFocus(fitbitSedentary, sittingTime, painTrend, stressLevel, loggedDays.size());

        //Now i detect patterns:
        List<String> detectPatterns = helper.detectPatterns(loggedDays, dto.getIsFitbitConnected());

        return WeeklySummaryResultDto.builder()
                .overAllSpineLoad(overAllSpineLoad)
                .loadCategory(loadCategory)
                .painTrend(painTrend)
                .activityBalance(activityBalance)
                .summaryText(summaryText)
                .improvements(improvements)
                .needsAttention(needsAttention)
                .detectedPatterns(detectPatterns)
                .nextWeekFocus(nextWeekFocus)
                .build();
    }
}
