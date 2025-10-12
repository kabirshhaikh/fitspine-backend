package com.fitspine.helper;

import com.fitspine.dto.FlareUpTriggersDto;
import com.fitspine.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
}
