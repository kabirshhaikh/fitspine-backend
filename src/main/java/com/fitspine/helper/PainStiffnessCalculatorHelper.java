package com.fitspine.helper;

import com.fitspine.dto.DailyGraphDto;
import com.fitspine.dto.DaySummaryDto;
import com.fitspine.dto.ExplanationDto;
import com.fitspine.dto.TrendResultDto;
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
                || day.getFitbitSedentaryHours() != null;
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
            boolean isFitbitConnected
    ) {
        List<ExplanationDto> explanations = new ArrayList<>();

        if (bestPainDay == null || worstPainDay == null) {
            return explanations;
        }

        DailyGraphDto best = findDayByDate(loggedDays, bestPainDay.getDate());
        DailyGraphDto worst = findDayByDate(loggedDays, worstPainDay.getDate());

        if (best == null || worst == null) {
            return explanations;
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
                            )
                            .build()
            );
        }

        return explanations;
    }
}
