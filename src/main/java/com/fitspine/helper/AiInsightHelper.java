package com.fitspine.helper;

import com.fitspine.model.AiDailyInsightFlareUpTriggers;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiInsightHelper {
    public List<String> returnTriggerText(List<AiDailyInsightFlareUpTriggers> list) {
        List<String> triggerText = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            AiDailyInsightFlareUpTriggers flareUpTriggerObject = list.get(i);
            triggerText.add(flareUpTriggerObject.getTriggerText());
        }

        return triggerText;
    }
}
