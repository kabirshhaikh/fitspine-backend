package com.fitspine.helper;

import com.fitspine.dto.FlareUpTriggersDto;
import com.fitspine.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiInsightHelper {
    public List<FlareUpTriggersDto> returnTriggerText(List<AiDailyInsightFlareUpTriggers> list) {
        List<FlareUpTriggersDto> flare = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            flare.add(FlareUpTriggersDto.builder()
                    .metric(list.get(i).getMetric())
                    .value(list.get(i).getValue())
                    .deviation(list.get(i).getDeviation())
                    .impact(list.get(i).getImpact())
                    .build()
            );
        }

        return flare;
    }

    public List<String> returnImprovedList(List<AiDailyInsightImproved> improvedList) {
        List<String> improved = new ArrayList<>();
        for (int i = 0; i < improvedList.size(); i++) {
            improved.add(improvedList.get(i).getImproved());
        }

        return improved;
    }

    public List<String> returnWorsenedList(List<AiDailyInsightWorsened> worsenedList) {
        List<String> worsened = new ArrayList<>();
        for (int i = 0; i < worsenedList.size(); i++) {
            worsened.add(worsenedList.get(i).getWorsened());
        }

        return worsened;
    }

    public List<String> returnPossibleCausesList(List<AiDailyInsightPossibleCauses> possibleCausesList) {
        List<String> possibleCauses = new ArrayList<>();
        for (int i = 0; i < possibleCausesList.size(); i++) {
            possibleCauses.add(possibleCausesList.get(i).getPossibleCauses());
        }

        return possibleCauses;
    }


    public List<String> returnActionableAdviceList(List<AiDailyInsightActionableAdvice> actionableAdviceList) {
        List<String> actionableAdvice = new ArrayList<>();
        for (int i = 0; i < actionableAdviceList.size(); i++) {
            actionableAdvice.add(actionableAdviceList.get(i).getAdvice());
        }
        return actionableAdvice;
    }

    public List<String> returnInterventionsTodayList(List<AiDailyInsightInterventionsToday> interventionsTodayList) {
        List<String> interventions = new ArrayList<>();
        for (int i = 0; i < interventionsTodayList.size(); i++) {
            interventions.add(interventionsTodayList.get(i).getInterventions());
        }
        return interventions;
    }

    public List<AiDailyInsightImproved> getImprovedList(List<String> improved, AiDailyInsight savedInsight) {
        List<AiDailyInsightImproved> improvedList = new ArrayList<>();

        for (int i = 0; i < improved.size(); i++) {
            improvedList.add(
                    AiDailyInsightImproved.builder()
                            .aiDailyInsight(savedInsight)
                            .improved(improved.get(i))
                            .build()
            );
        }

        return improvedList;
    }

    public List<AiDailyInsightWorsened> getWorsened(List<String> worsened, AiDailyInsight savedInsight) {
        List<AiDailyInsightWorsened> worsenedList = new ArrayList<>();
        for (int i = 0; i < worsened.size(); i++) {
            worsenedList.add(
                    AiDailyInsightWorsened.builder()
                            .aiDailyInsight(savedInsight)
                            .worsened(worsened.get(i))
                            .build()
            );
        }

        return worsenedList;
    }

    public List<AiDailyInsightPossibleCauses> getPossibleIssues(List<String> possibleCauses, AiDailyInsight savedInsight) {
        List<AiDailyInsightPossibleCauses> possibleCausesList = new ArrayList<>();
        for (int i = 0; i < possibleCauses.size(); i++) {
            possibleCausesList.add(
                    AiDailyInsightPossibleCauses.builder()
                            .aiDailyInsight(savedInsight)
                            .possibleCauses(possibleCauses.get(i))
                            .build()
            );
        }

        return possibleCausesList;
    }

    public List<AiDailyInsightActionableAdvice> getActionableAdvice(List<String> actionableAdvice, AiDailyInsight savedInsight) {
        List<AiDailyInsightActionableAdvice> actionableAdvicesList = new ArrayList<>();
        for (int i = 0; i < actionableAdvice.size(); i++) {
            actionableAdvicesList.add(
                    AiDailyInsightActionableAdvice.builder()
                            .aiDailyInsight(savedInsight)
                            .advice(actionableAdvice.get(i))
                            .build()
            );
        }

        return actionableAdvicesList;
    }

    public List<AiDailyInsightInterventionsToday> getInterventionsToday(List<String> interventionsToday, AiDailyInsight savedInsight) {
        List<AiDailyInsightInterventionsToday> interventionsTodaysList = new ArrayList<>();
        for (int i = 0; i < interventionsToday.size(); i++) {
            interventionsTodaysList.add(
                    AiDailyInsightInterventionsToday.builder()
                            .aiDailyInsight(savedInsight)
                            .interventions(interventionsToday.get(i))
                            .build()
            );
        }

        return interventionsTodaysList;
    }

    public List<AiDailyInsightFlareUpTriggers> getFlareUpTriggers(List<FlareUpTriggersDto> flareUpTriggersDtos, AiDailyInsight savedInsight) {
        List<AiDailyInsightFlareUpTriggers> flareUpEntries = new ArrayList<>();
        for (int i = 0; i < flareUpTriggersDtos.size(); i++) {
            flareUpEntries.add(
                    AiDailyInsightFlareUpTriggers.builder()
                            .aiDailyInsight(savedInsight)
                            .metric(flareUpTriggersDtos.get(i).getMetric())
                            .value(flareUpTriggersDtos.get(i).getValue())
                            .deviation(flareUpTriggersDtos.get(i).getDeviation())
                            .impact(flareUpTriggersDtos.get(i).getImpact())
                            .build()
            );
        }

        return flareUpEntries;
    }

    public HashMap<String, Integer> getActivityLogMap(List<FitbitActivitiesLog> activitiesLog) {
        HashMap<String, Integer> outputMap = new HashMap<>();

        if (activitiesLog == null || activitiesLog.isEmpty()) {
            return outputMap;
        }

        for (int i = 0; i < activitiesLog.size(); i++) {
            FitbitActivitiesLog current = activitiesLog.get(i);
            if (current != null && current.getActivityParentName() != null && current.getCalories() != null) {
                outputMap.put(current.getActivityParentName(), current.getCalories());
            }
        }

        return outputMap;
    }

    public String getHumanReadableDescription(HashMap<String, Integer> map) {
        if (map.isEmpty()) {
            return "No description found for the activity";
        }

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> myMap : map.entrySet()) {
            String key = myMap.getKey();
            Integer value = myMap.getValue();
            if (key != null && value != null) {
                sb.append(key).append(" (").append(value).append(" kcal)").append(", ");
            }
        }

        String result = sb.toString().trim();
        return result.endsWith(",") ? result.substring(0, result.length() - 1) : result;
    }
}
