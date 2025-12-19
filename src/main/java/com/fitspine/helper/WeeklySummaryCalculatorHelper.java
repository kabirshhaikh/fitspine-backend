package com.fitspine.helper;

import com.fitspine.dto.DailyGraphDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WeeklySummaryCalculatorHelper {
    public List<DailyGraphDto> getLoggedDays(List<DailyGraphDto> allDays) {
        List<DailyGraphDto> loggedDaysList = new ArrayList<>();

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto currentDay = allDays.get(i);
            if (hasAnyData(currentDay)) {
                loggedDaysList.add(currentDay);
            }
        }

        return loggedDaysList;
    }

    public boolean hasAnyData(DailyGraphDto day) {

        return isValid(day.getPainLevel())
                || isValid(day.getMorningStiffness())
                || isValid(day.getStressLevel())
                || isValid(day.getSittingTime())
                || isValid(day.getStandingTime())
                || day.getManualRestingHeartRate() != null
                || day.getFitbitRestingHeartRate() != null
                || day.getFitbitSedentaryHours() != null;
    }

    public boolean isValid(Integer value) {
        return value != null && value != -1;
    }

    //For all the enum values like: pain level, morning stiffness, standing, sitting time and stress values list:
    public Double calculateAverage(List<Integer> values) {
        int sum = 0;
        int count = 0;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) != null && values.get(i) != -1) {
                sum = sum + values.get(i);
                count++;
            }
        }

        return count == 0 ? null : (double) sum / count;
    }

    //For fields which have data type of Double and not Integer:
    public Double calculateDoubleAverage(List<Double> values) {
        double sum = 0;
        int count = 0;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) != null) {
                sum = sum + values.get(i);
                count++;
            }
        }

        return count == 0 ? null : (double) sum / count;
    }

    public List<Double> addComponentsToSpineOverLoadArray(Double painLevel, Double morningStiffness, Double sittingTime, Double stressLevel, Double fitbitSedentary) {
        List<Double> spineLoadComponents = new ArrayList<>();

        if (painLevel != null) {
            spineLoadComponents.add(painLevel);
        }

        if (morningStiffness != null) {
            spineLoadComponents.add(morningStiffness);
        }

        if (sittingTime != null) {
            spineLoadComponents.add(sittingTime);
        }

        if (stressLevel != null) {
            spineLoadComponents.add(stressLevel);
        }

        //normalize sedentary hours into the same 0–4
        // impact scale as pain, stiffness, and sitting, so no single metric overwhelms the overall spine load.
        if (fitbitSedentary != null) {
            spineLoadComponents.add(fitbitSedentary / 3.0);
        }

        return spineLoadComponents;
    }

    public Double calculateOverallSpineLoad(List<Double> spineLoadComponent) {
        double sum = 0;

        for (int i = 0; i < spineLoadComponent.size(); i++) {
            sum = sum + spineLoadComponent.get(i);
        }

        return sum / spineLoadComponent.size();
    }

    public String returnLoadCategoryString(Double overAllLoad) {
        if (overAllLoad < 1.0) {
            return "low";
        } else if (overAllLoad <= 2.0) {
            return "moderate";
        } else {
            return "high";
        }
    }

    public List<String> getListOfImprovements(DailyGraphDto firstDay, DailyGraphDto lastDay) {
        List<String> improvements = new ArrayList<>();

        if (firstDay == null || lastDay == null) {
            return improvements;
        }

        // Pain decreased
        if (isValid(firstDay.getPainLevel()) && isValid(lastDay.getPainLevel())
                && lastDay.getPainLevel() < firstDay.getPainLevel()) {
            improvements.add("Pain score decreased");
        }

        // Morning stiffness decreased
        if (isValid(firstDay.getMorningStiffness()) && isValid(lastDay.getMorningStiffness())
                && lastDay.getMorningStiffness() < firstDay.getMorningStiffness()) {
            improvements.add("Morning stiffness decreased");
        }

        // Stress decreased
        if (isValid(firstDay.getStressLevel()) && isValid(lastDay.getStressLevel())
                && lastDay.getStressLevel() < firstDay.getStressLevel()) {
            improvements.add("Stress level decreased");
        }

        // Sitting time reduced
        if (isValid(firstDay.getSittingTime()) && isValid(lastDay.getSittingTime())
                && lastDay.getSittingTime() < firstDay.getSittingTime()) {
            improvements.add("Sitting time reduced");
        }

        // Standing time increased
        if (isValid(firstDay.getStandingTime()) && isValid(lastDay.getStandingTime())
                && lastDay.getStandingTime() > firstDay.getStandingTime()) {
            improvements.add("Standing time increased");
        }

        // Sedentary hours reduced (Fitbit)
        if (firstDay.getFitbitSedentaryHours() != null && lastDay.getFitbitSedentaryHours() != null
                && lastDay.getFitbitSedentaryHours() < firstDay.getFitbitSedentaryHours()) {
            improvements.add("Sedentary hours reduced");
        }

        return improvements;
    }

    public List<String> getListOfNeedsAttention(Double fitbitSedentary, DailyGraphDto firstDay, DailyGraphDto lastDay, int allDays, int loggedDays, Double painLevel, Double stressLevel, boolean isNewUser) {
        List<String> needsAttention = new ArrayList<>();

        //High sedentary hours: fitbit:
        if (fitbitSedentary != null && fitbitSedentary > 11.0) {
            needsAttention.add(
                    String.format("High sedentary time (%.1f hrs average)", fitbitSedentary)
            );
        }

        //Sitting time increased over the week
        if (firstDay != null && lastDay != null
                && isValid(firstDay.getSittingTime())
                && isValid(lastDay.getSittingTime())
                && lastDay.getSittingTime() > firstDay.getSittingTime()) {

            needsAttention.add("Sitting time increased this week");
        }

        //Missing logs (with new-user protection)
        int missingCount = allDays - loggedDays;

        if (missingCount > 0) {
            if (isNewUser) {
                needsAttention.add(
                        missingCount + " days with missing logs (expected for new users)"
                );
            } else {
                needsAttention.add(
                        missingCount + " days with missing logs"
                );
            }
        }

        //Elevated pain
        if (painLevel != null && painLevel >= 2.0) {
            needsAttention.add(
                    String.format("Elevated pain levels (average: %.1f)", painLevel)
            );
        }

        //High stress
        if (stressLevel != null && stressLevel >= 3.0) {
            needsAttention.add(
                    String.format("High stress levels (average: %.1f)", stressLevel)
            );
        }

        return needsAttention;
    }

    public String getNextWeeksFocus(Double fitbitSedentary, Double sittingTime, String painTrend, Double stressLevel, int loggedDays) {
        String nextWeekFocus = null;

        //Priority 1: High sedentary hours:
        if (fitbitSedentary != null && fitbitSedentary > 11.0) {
            nextWeekFocus =
                    "Reducing sedentary time below 10 hours may help maintain lower pain levels";
        }

        //Priority 2: High sitting time -> 6–8h or more:
        else if (sittingTime != null && sittingTime >= 3.0) {
            nextWeekFocus =
                    "Increasing standing time and reducing sitting duration may support spine health";
        }

        //Priority 3: Worsening pain trend:
        else if ("worsening".equals(painTrend)) {
            nextWeekFocus =
                    "Focus on reducing sedentary time and increasing gentle movement to address rising pain levels";
        }

        //Priority 4: High stress:
        else if (stressLevel != null && stressLevel >= 3.0) {
            nextWeekFocus =
                    "Managing stress levels may help improve recovery and overall spine comfort";
        }

        //Priority 5: Too few logged days:
        else if (loggedDays < 4) {
            nextWeekFocus =
                    "Consistent daily logging will help identify patterns and track improvements";
        }

        //Priority 6: Default
        else if ("improving".equals(painTrend)) {
            nextWeekFocus =
                    "Continue current activity patterns to maintain the improving pain trend";
        } else {
            nextWeekFocus =
                    "Maintain balanced activity levels and continue monitoring pain patterns";
        }

        return nextWeekFocus;
    }

    public List<String> detectPatterns(List<DailyGraphDto> loggedDays, boolean isFitbitConnected) {
        List<String> patterns = new ArrayList<>();

        if (loggedDays.size() < 2) {
            return patterns;
        }


        //Sedentary hours vs pain
        List<DailyGraphDto> sedentaryPainDays = new ArrayList<>();
        for (DailyGraphDto d : loggedDays) {
            if (d.getFitbitSedentaryHours() != null && isValid(d.getPainLevel())) {
                sedentaryPainDays.add(d);
            }
        }

        if (sedentaryPainDays.size() >= 2) {
            sedentaryPainDays.sort((a, b) ->
                    Double.compare(a.getFitbitSedentaryHours(), b.getFitbitSedentaryHours()));

            int mid = sedentaryPainDays.size() / 2;
            double lowPainAvg = 0, highPainAvg = 0;
            int lowCount = 0, highCount = 0;

            for (int i = 0; i < sedentaryPainDays.size(); i++) {
                if (i < mid) {
                    lowPainAvg += sedentaryPainDays.get(i).getPainLevel();
                    lowCount++;
                } else {
                    highPainAvg += sedentaryPainDays.get(i).getPainLevel();
                    highCount++;
                }
            }

            lowPainAvg /= lowCount;
            highPainAvg /= highCount;

            if (highPainAvg > lowPainAvg + 0.3) {
                patterns.add("Higher sedentary hours appear to align with increased pain levels");
            }
        }


        //Sitting time vs pain

        List<DailyGraphDto> sittingPainDays = new ArrayList<>();
        for (DailyGraphDto d : loggedDays) {
            if (isValid(d.getSittingTime()) && isValid(d.getPainLevel())) {
                sittingPainDays.add(d);
            }
        }

        if (sittingPainDays.size() >= 2) {
            sittingPainDays.sort((a, b) ->
                    Integer.compare(a.getSittingTime(), b.getSittingTime()));

            int mid = sittingPainDays.size() / 2;
            double lowPainAvg = 0, highPainAvg = 0;
            int lowCount = 0, highCount = 0;

            for (int i = 0; i < sittingPainDays.size(); i++) {
                if (i < mid) {
                    lowPainAvg += sittingPainDays.get(i).getPainLevel();
                    lowCount++;
                } else {
                    highPainAvg += sittingPainDays.get(i).getPainLevel();
                    highCount++;
                }
            }

            lowPainAvg /= lowCount;
            highPainAvg /= highCount;

            if (highPainAvg > lowPainAvg + 0.3) {
                patterns.add("Lower sitting time coincided with improved pain levels");
            }
        }


        //Stress vs heart rate
        List<DailyGraphDto> stressHrDays = new ArrayList<>();
        for (DailyGraphDto d : loggedDays) {
            Integer hr = isFitbitConnected
                    ? d.getFitbitRestingHeartRate()
                    : d.getManualRestingHeartRate();

            if (isValid(d.getStressLevel()) && hr != null) {
                stressHrDays.add(d);
            }
        }

        if (stressHrDays.size() >= 2) {
            stressHrDays.sort((a, b) ->
                    Integer.compare(a.getStressLevel(), b.getStressLevel()));

            int mid = stressHrDays.size() / 2;
            double lowHrAvg = 0, highHrAvg = 0;
            int lowCount = 0, highCount = 0;

            for (int i = 0; i < stressHrDays.size(); i++) {
                Integer hr = isFitbitConnected
                        ? stressHrDays.get(i).getFitbitRestingHeartRate()
                        : stressHrDays.get(i).getManualRestingHeartRate();

                if (i < mid) {
                    lowHrAvg += hr;
                    lowCount++;
                } else {
                    highHrAvg += hr;
                    highCount++;
                }
            }

            lowHrAvg /= lowCount;
            highHrAvg /= highCount;

            if (highHrAvg > lowHrAvg + 2) {
                patterns.add("Lower stress levels appeared to align with lower resting heart rate");
            }
        }

        return patterns;
    }
}
