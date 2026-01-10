package com.fitspine.helper;

import com.fitspine.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
@Slf4j
public class PainStiffnessCalculatorHelper {
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
                || day.getFitbitSedentaryHours() != null
                || day.getFlareUp() != null;
    }

    public boolean isValid(Integer value) {
        return value != null && value != -1;
    }

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

    public TrendResultDto calculateTrendFromValues(List<Integer> values, boolean lowerIsBetter) {
        //Filter valid values:
        List<Integer> validValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) != null && values.get(i) != -1) {
                validValues.add(values.get(i));
            }
        }

        //Need atleast 2 values:
        if (validValues.size() < 2) {
            return null;
        }

        //Split into early v/s later part of week:
        int mid = validValues.size() / 2;
        List<Integer> firstHalf = validValues.subList(0, mid);
        List<Integer> secondHalf = validValues.subList(mid, validValues.size());


        //Avoid empty halves:
        if (firstHalf.isEmpty() || secondHalf.isEmpty()) {
            return null;
        }

        Double firstHalfAverage = this.calculateAverage(firstHalf);
        Double secondHalfAverage = this.calculateAverage(secondHalf);

        //Calculate percentage change:
        double difference = secondHalfAverage - firstHalfAverage;
        double percentageChange;

        if (firstHalfAverage == 0) {
            percentageChange = 0;
        } else {
            percentageChange = Math.abs((difference / firstHalfAverage) * 100);
        }

        //Stability threshold: i have set it to 5%:
        if (percentageChange < 5) {
            return TrendResultDto.builder()
                    .direction("stable")
                    .changePercentage(0)
                    .build();
        }

        //Otherwise determine the direction:
        String direction;
        if (lowerIsBetter) {
            direction = difference < 0 ? "better" : "worse";
        } else {
            direction = difference > 0 ? "better" : "worse";
        }

        return TrendResultDto.builder()
                .direction(direction)
                .changePercentage(Math.round(percentageChange * 10.0) / 10.0)
                .build();
    }

    public DaySummaryDto getBestDay(List<DailyGraphDto> loggedDays, Function<DailyGraphDto, Integer> extractor) {
        Integer bestValue = null;
        String bestDate = null;

        for (int i = 0; i < loggedDays.size(); i++) {
            DailyGraphDto currentDay = loggedDays.get(i);
            Integer value = extractor.apply(currentDay);

            if (value == null || value == -1 || currentDay.getDate() == null) {
                continue;
            }

            if (bestValue == null || value < bestValue) {
                bestValue = value;
                bestDate = currentDay.getDate();
            }
        }

        if (bestValue == null) {
            return null;

        }

        return DaySummaryDto.builder()
                .value(bestValue)
                .date(bestDate)
                .build();
    }

    public DaySummaryDto getWorstDay(List<DailyGraphDto> loggedDays, Function<DailyGraphDto, Integer> extractor) {
        Integer worstValue = null;
        String worstDate = null;

        for (int i = 0; i < loggedDays.size(); i++) {
            DailyGraphDto currentDay = loggedDays.get(i);
            Integer value = extractor.apply(currentDay);

            if (value == null || value == -1 || currentDay.getDate() == null) {
                continue;
            }

            if (worstValue == null || value > worstValue) {
                worstValue = value;
                worstDate = currentDay.getDate();
            }
        }

        if (worstValue == null) {
            return null;

        }

        return DaySummaryDto.builder()
                .value(worstValue)
                .date(worstDate)
                .build();
    }

    public List<String> detectPainCorrelation(List<DailyGraphDto> loggedDays, boolean isFitbitConnected) {
        List<String> correlations = new ArrayList<>();

        //Pain v/s Standing time:
        List<DailyGraphDto> standingPainDays = new ArrayList<>();

        for (int i = 0; i < loggedDays.size(); i++) {
            DailyGraphDto current = loggedDays.get(i);
            if (isValid(current.getPainLevel()) && isValid(current.getStandingTime())) {
                standingPainDays.add(current);
            }
        }

        if (standingPainDays.size() >= 2) {
            standingPainDays.sort((a, b) ->
                    Integer.compare(a.getStandingTime(), b.getStandingTime()));

            int firstHalfSize = (int) Math.ceil(standingPainDays.size() / 2.0);
            int secondHalfSize = standingPainDays.size() - firstHalfSize;

            double lowPain = 0, highPain = 0;

            for (int j = 0; j < standingPainDays.size(); j++) {
                if (j < firstHalfSize) {
                    lowPain = lowPain + standingPainDays.get(j).getPainLevel();

                } else {
                    highPain = highPain + standingPainDays.get(j).getPainLevel();

                }
            }

            if (firstHalfSize > 0 && secondHalfSize > 0) {
                double avgPainLow = lowPain / firstHalfSize;
                double avgPainHigh = highPain / secondHalfSize;

                if (avgPainLow > avgPainHigh + 0.3) {
                    double percentReduction = ((avgPainLow - avgPainHigh) / avgPainLow) * 100;

                    correlations.add(String.format(
                            "Pain was %d%% lower on days with more standing time",
                            Math.round(percentReduction)
                    ));
                }
            }
        }

        //Pain v/s sedentary hours (for fitbit users only):
        if (isFitbitConnected) {
            List<DailyGraphDto> sedentaryPainDays = new ArrayList<>();
            for (int i = 0; i < loggedDays.size(); i++) {
                DailyGraphDto current = loggedDays.get(i);
                if (isValid(current.getPainLevel()) && current.getFitbitSedentaryHours() != null) {
                    sedentaryPainDays.add(current);
                }
            }

            if (sedentaryPainDays.size() >= 2) {
                sedentaryPainDays.sort((a, b) ->
                        Double.compare(
                                a.getFitbitSedentaryHours(),
                                b.getFitbitSedentaryHours()
                        ));

                int firstHalfSize = (int) Math.ceil(sedentaryPainDays.size() / 2.0);
                int secondHalfSize = sedentaryPainDays.size() - firstHalfSize;

                double lowPain = 0, highPain = 0;
                double sedentarySum = 0;

                for (int j = 0; j < sedentaryPainDays.size(); j++) {
                    if (j < firstHalfSize) {
                        lowPain += sedentaryPainDays.get(j).getPainLevel();
                    } else {
                        highPain += sedentaryPainDays.get(j).getPainLevel();
                        sedentarySum = sedentarySum + sedentaryPainDays.get(j).getFitbitSedentaryHours();
                    }
                }

                if (firstHalfSize > 0 && secondHalfSize > 0) {
                    double avgPainLow = lowPain / firstHalfSize;
                    double avgPainHigh = highPain / secondHalfSize;

                    if (avgPainHigh > avgPainLow + 0.3) {
                        double threshold = sedentarySum / secondHalfSize;

                        correlations.add(String.format(
                                "Pain increased when sedentary hours exceeded %.1fhrs",
                                threshold
                        ));
                    }
                }
            }
        }

        return correlations;
    }

    public DailyGraphDto findDayByDate(List<DailyGraphDto> loggedDays, String date) {
        for (int i = 0; i < loggedDays.size(); i++) {
            DailyGraphDto day = loggedDays.get(i);
            if (date != null && date.equals(day.getDate())) {
                return day;
            }
        }

        return null;
    }

    public List<ExplanationDto> explainPainChange(
            DaySummaryDto bestPainDay,
            DaySummaryDto worstPainDay,
            List<DailyGraphDto> loggedDays,
            boolean isFitbitConnected,
            List<String> userDiscIssues,
            List<String> userInjuries
    ) {
        List<ExplanationDto> explanations = new ArrayList<>();

        if (bestPainDay == null || worstPainDay == null) {
            return explanations;
        }

        //Only explain if pain actually worsened
        if (worstPainDay.getValue() <= bestPainDay.getValue()) {
            return explanations;
        }

        DailyGraphDto best = findDayByDate(loggedDays, bestPainDay.getDate());
        DailyGraphDto worst = findDayByDate(loggedDays, worstPainDay.getDate());

        if (best == null || worst == null) {
            return explanations;
        }

        String discContextSuffix = "";
        String injuryContextSuffix = "";

        if (userDiscIssues != null && !userDiscIssues.isEmpty()) {
            discContextSuffix =
                    " In individuals with disc-related conditions such as "
                            + String.join(", ", userDiscIssues)
                            + ", pain sensitivity may be higher during periods of increased load or inactivity.";
        }

        if (userInjuries != null && !userInjuries.isEmpty()) {
            injuryContextSuffix =
                    " A history of spinal injury can further increase vulnerability to pain flare-ups.";
        }

        //Flare up:
        if (Boolean.TRUE.equals(worst.getFlareUp())) {
            String compare = "";

            if (Boolean.FALSE.equals(best.getFlareUp())) {
                compare = " The best day was not marked as a flare-up, so this is a meaningful difference.";
            }

            explanations.add(
                    ExplanationDto.builder()
                            .cause("A flare-up was recorded on the high-pain day")
                            .explanation(
                                    "During flare-ups, pain can become more sensitive and easier to trigger. "
                                            + "This can amplify pain even with small changes in activity, sleep, or stress."
                                            + compare
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }

        //Sedentary hours if fitbit is connected:
        if (isFitbitConnected
                && best.getFitbitSedentaryHours() != null
                && worst.getFitbitSedentaryHours() != null
                && worst.getFitbitSedentaryHours() > best.getFitbitSedentaryHours() + 1) {

            explanations.add(
                    ExplanationDto.builder()
                            .cause(String.format(
                                    "Sedentary hours were %.1f hrs vs %.1f hrs",
                                    worst.getFitbitSedentaryHours(),
                                    best.getFitbitSedentaryHours()
                            ))
                            .explanation(
                                    "Prolonged sedentary time increases spinal load and reduces circulation, "
                                            + "which can contribute to increased discomfort."
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }


        //Standing time (lower is worse):
        if (isValid(best.getStandingTime())
                && isValid(worst.getStandingTime())
                && worst.getStandingTime() < best.getStandingTime()) {

            explanations.add(
                    ExplanationDto.builder()
                            .cause("Standing time was lower on high-pain days")
                            .explanation(
                                    "Reduced standing time decreases muscle activation and circulation, "
                                            + "which can increase stiffness and discomfort."
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }

        //Stress level (higher is worse):
        if (isValid(best.getStressLevel())
                && isValid(worst.getStressLevel())
                && worst.getStressLevel() > best.getStressLevel()) {

            explanations.add(
                    ExplanationDto.builder()
                            .cause("Higher stress levels were recorded on high-pain days")
                            .explanation(
                                    "Elevated stress increases muscle tension and lowers pain tolerance, "
                                            + "which can amplify discomfort."
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }


        //Sleep duration (Fitbit + Manual):
        Double bestSleepHours = null;
        Double worstSleepHours = null;

        if (isFitbitConnected) {
            if (best.getFitbitTotalMinutesAsleep() != null) {
                bestSleepHours = best.getFitbitTotalMinutesAsleep() / 60.0;
            }
            if (worst.getFitbitTotalMinutesAsleep() != null) {
                worstSleepHours = worst.getFitbitTotalMinutesAsleep() / 60.0;
            }
        } else {
            // Manual sleep enum → hours
            if (isValid(best.getSleepDuration()) && isValid(worst.getSleepDuration())) {
                double[] sleepMap = {4.5, 5.5, 6.5, 7.5, 8.5};
                bestSleepHours = sleepMap[best.getSleepDuration()];
                worstSleepHours = sleepMap[worst.getSleepDuration()];
            }
        }

        if (bestSleepHours != null
                && worstSleepHours != null
                && worstSleepHours < bestSleepHours - 0.5) {

            explanations.add(
                    ExplanationDto.builder()
                            .cause(String.format(
                                    "Sleep duration was %.1fh vs %.1fh",
                                    worstSleepHours,
                                    bestSleepHours
                            ))
                            .explanation(
                                    "Insufficient sleep reduces the body's ability to recover and increases "
                                            + "sensitivity to discomfort."
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }

        return explanations;
    }

    public List<ExplanationDto> explainStiffnessChange(
            DaySummaryDto bestMorningStiffnessDay,
            DaySummaryDto worstMorningStiffnessDay,
            List<DailyGraphDto> loggedDays,
            boolean isFitbitConnected,
            List<String> userDiscIssues,
            List<String> userInjuries
    ) {
        List<ExplanationDto> explanations = new ArrayList<>();

        if (bestMorningStiffnessDay == null || worstMorningStiffnessDay == null) {
            return explanations;
        }

        //Only explain if stiffness actually worsened
        if (worstMorningStiffnessDay.getValue() <= bestMorningStiffnessDay.getValue()) {
            return explanations;
        }

        DailyGraphDto best = findDayByDate(loggedDays, bestMorningStiffnessDay.getDate());
        DailyGraphDto worst = findDayByDate(loggedDays, worstMorningStiffnessDay.getDate());

        if (best == null || worst == null) {
            return explanations;
        }

        String discContextSuffix = "";
        String injuryContextSuffix = "";

        if (userDiscIssues != null && !userDiscIssues.isEmpty()) {
            discContextSuffix =
                    " In individuals with disc-related conditions such as "
                            + String.join(", ", userDiscIssues)
                            + ", symptom sensitivity may be higher.";
        }

        if (userInjuries != null && !userInjuries.isEmpty()) {
            injuryContextSuffix =
                    " A history of spinal injury can further increase susceptibility to stiffness.";
        }

        //Flare up:
        if (Boolean.TRUE.equals(worst.getFlareUp())) {
            String compare = "";

            if (Boolean.FALSE.equals(best.getFlareUp())) {
                compare = " The best day was not marked as a flare-up, so this is a meaningful difference.";
            }

            explanations.add(
                    ExplanationDto.builder()
                            .cause("A flare-up was recorded on the high-stiffness day")
                            .explanation(
                                    "During flare-ups, stiffness can increase due to protective muscle guarding and reduced movement. "
                                            + "Gentle movement and reducing long sitting blocks usually help."
                                            + compare
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }

        //Sedentary hours if fitbit is connected:
        if (isFitbitConnected
                && best.getFitbitSedentaryHours() != null
                && worst.getFitbitSedentaryHours() != null
                && worst.getFitbitSedentaryHours() > best.getFitbitSedentaryHours() + 1) {

            explanations.add(
                    ExplanationDto.builder()
                            .cause(String.format(
                                    "Sedentary hours was %.1fhrs (vs %.1fhrs on best day)",
                                    worst.getFitbitSedentaryHours(),
                                    best.getFitbitSedentaryHours()
                            ))
                            .explanation(
                                    "Prolonged sedentary time increases spinal load and reduces circulation, "
                                            + "which can contribute to increased stiffness."
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }


        //Standing time:
        if (isValid(best.getStandingTime())
                && isValid(worst.getStandingTime())
                && worst.getStandingTime() < best.getStandingTime()) {

            explanations.add(
                    ExplanationDto.builder()
                            .cause(String.format(
                                    "Standing time was %s (vs %s on best day)",
                                    EnumScoreHelper.enumToTimeLabel(worst.getStandingTime()),
                                    EnumScoreHelper.enumToTimeLabel(best.getStandingTime())
                            ))
                            .explanation(
                                    "Reduced standing time decreases muscle activation and circulation, "
                                            + "which can lead to increased stiffness."
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }

        //Stress level:
        if (isValid(best.getStressLevel())
                && isValid(worst.getStressLevel())
                && worst.getStressLevel() > best.getStressLevel()) {

            explanations.add(
                    ExplanationDto.builder()
                            .cause(String.format(
                                    "Stress level was %s (vs %s on best day)",
                                    EnumScoreHelper.enumToStressLabel(worst.getStressLevel()),
                                    EnumScoreHelper.enumToStressLabel(best.getStressLevel())
                            ))
                            .explanation(
                                    "Higher stress levels increase muscle tension, which can worsen stiffness."
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }

        //Sleep duration:
        Double bestSleepHours = null;
        Double worstSleepHours = null;

        if (isFitbitConnected) {
            if (best.getFitbitTotalMinutesAsleep() != null) {
                bestSleepHours = best.getFitbitTotalMinutesAsleep() / 60.0;
            }
            if (worst.getFitbitTotalMinutesAsleep() != null) {
                worstSleepHours = worst.getFitbitTotalMinutesAsleep() / 60.0;
            }
        } else if (isValid(best.getSleepDuration()) && isValid(worst.getSleepDuration())) {
            double[] sleepMap = {4.5, 5.5, 6.5, 7.5, 8.5};
            bestSleepHours = sleepMap[best.getSleepDuration()];
            worstSleepHours = sleepMap[worst.getSleepDuration()];
        }

        if (bestSleepHours != null
                && worstSleepHours != null
                && worstSleepHours < bestSleepHours - 0.5) {

            explanations.add(
                    ExplanationDto.builder()
                            .cause(String.format(
                                    "Sleep duration was %.1fh (vs %.1fh on best day)",
                                    worstSleepHours,
                                    bestSleepHours
                            ))
                            .explanation(
                                    "Insufficient sleep reduces tissue recovery and increases morning stiffness."
                                            + discContextSuffix
                                            + injuryContextSuffix
                            )
                            .build()
            );
        }

        return explanations;
    }

    public List<DailyGraphDto> getFlareUpDays(List<DailyGraphDto> loggedDays) {
        List<DailyGraphDto> flareUpDays = new ArrayList<>();

        if (loggedDays == null || loggedDays.isEmpty()) {
            return flareUpDays;
        }

        for (int i = 0; i < loggedDays.size(); i++) {
            DailyGraphDto day = loggedDays.get(i);

            if (day != null && Boolean.TRUE.equals(day.getFlareUp())) {
                flareUpDays.add(day);
            }
        }

        return flareUpDays;
    }

    public List<DailyGraphDto> filterDaysWithValidMetric(List<DailyGraphDto> days, Function<DailyGraphDto, Integer> extractor) {
        List<DailyGraphDto> valid = new ArrayList<>();

        if (days == null || days.isEmpty()) {
            return valid;
        }

        for (int i = 0; i < days.size(); i++) {
            DailyGraphDto currentDay = days.get(i);
            if (currentDay == null) continue;

            Integer value = extractor.apply(currentDay);
            if (isValid(value) && currentDay.getDate() != null) {
                valid.add(currentDay);
            }
        }

        return valid;
    }

    public List<PainDailyBreakDownDto> getDailyBreakDown(List<DailyGraphDto> allDays) {
        List<PainDailyBreakDownDto> list = new ArrayList<>();

        if (allDays == null || allDays.isEmpty()) {
            return list;
        }

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);
            if (day == null) {
                continue;
            }

            String pain = day.getPainLevel() != null
                    ? EnumScoreHelper.painLabel(day.getPainLevel())
                    : null;

            String stiffness = day.getMorningStiffness() != null
                    ? EnumScoreHelper.morningStiffnessLabel(day.getMorningStiffness())
                    : null;

            list.add(
                    PainDailyBreakDownDto.builder()
                            .logDate(day.getDate())
                            .painLevel(pain)
                            .stiffnessLevel(stiffness)
                            .build()
            );
        }


        return list;
    }

}
