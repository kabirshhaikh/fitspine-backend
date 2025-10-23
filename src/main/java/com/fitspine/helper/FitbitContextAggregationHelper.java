package com.fitspine.helper;

import com.fitspine.dto.FitbitActivityGoalsLogMetricDto;
import com.fitspine.dto.FitbitActivitySummariesMetricDto;
import com.fitspine.dto.FitbitSleepLogMetricDto;
import com.fitspine.dto.FitbitSleepSummaryLogMetricDto;
import com.fitspine.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class FitbitContextAggregationHelper {
    //Helper functions to return list of fitbit normalized metrics:
    public List<Integer> getPainLevels(List<ManualDailyLog> manualDailyLogs) {
        List<Integer> painLevels = new ArrayList<>();

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null || log.getPainLevel() == null) {
                painLevels.add(null);
            } else {
                painLevels.add(EnumScoreHelper.pain(log.getPainLevel()));
            }
        }

        return painLevels;
    }

    public List<Integer> getMorningStiffness(List<ManualDailyLog> manualDailyLogs) {
        List<Integer> morningStiffness = new ArrayList<>();

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null || log.getMorningStiffness() == null) {
                morningStiffness.add(null);
            } else {
                morningStiffness.add(EnumScoreHelper.morningStiffness(log.getMorningStiffness()));
            }
        }

        return morningStiffness;
    }

    public List<Integer> getSittingTime(List<ManualDailyLog> manualDailyLogs) {
        List<Integer> sittingTime = new ArrayList<>();

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null || log.getSittingTime() == null) {
                sittingTime.add(null);
            } else {
                sittingTime.add(EnumScoreHelper.sittingTime(log.getSittingTime()));
            }
        }

        return sittingTime;
    }

    public List<Integer> getStandingTime(List<ManualDailyLog> manualDailyLogs) {
        List<Integer> standingTime = new ArrayList<>();

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null || log.getStandingTime() == null) {
                standingTime.add(null);
            } else {
                standingTime.add(EnumScoreHelper.standingTime(log.getStandingTime()));
            }
        }

        return standingTime;
    }

    public List<Integer> getStressLevel(List<ManualDailyLog> manualDailyLogs) {
        List<Integer> stressLevel = new ArrayList<>();

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null || log.getStressLevel() == null) {
                stressLevel.add(null);
            } else {
                stressLevel.add(EnumScoreHelper.stressLevel(log.getStressLevel()));
            }
        }

        return stressLevel;
    }

    public List<Double> getSedentaryHours(List<FitbitActivitySummariesLog> activitySummariesLogs) {
        List<Double> sedentaryHours = new ArrayList<>();

        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);

            if (log == null || log.getSedentaryMinutes() == null) {
                sedentaryHours.add(null);
            } else {
                int sedentaryMinutes = log.getSedentaryMinutes();
                double hours = Math.round((sedentaryMinutes / 60.0) * 10.0) / 10.0;
                sedentaryHours.add(hours);
            }
        }

        return sedentaryHours;
    }

    public List<String> getDatesForWeeklyGraph(LocalDate startDate) {
        List<String> dates = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            dates.add(startDate.plusDays(i).toString());
        }

        return dates;
    }

    public List<Integer> getRestingHeartRateForWeeklyGraph(List<FitbitActivitiesHeartLog> heartLogs) {
        List<Integer> restingHeartRates = new ArrayList<>();

        if (heartLogs == null || heartLogs.isEmpty()) {
            return restingHeartRates;
        }

        for (int i = 0; i < heartLogs.size(); i++) {
            FitbitActivitiesHeartLog heartLog = heartLogs.get(i);
            if (heartLog == null || heartLog.getValues() == null) continue;

            List<FitbitActivitiesHeartValueLog> valueLogs = heartLog.getValues();
            if (valueLogs == null || valueLogs.isEmpty()) continue;

            for (int j = 0; j < valueLogs.size(); j++) {
                FitbitActivitiesHeartValueLog heartValueLog = valueLogs.get(j);
                if (heartValueLog == null) {
                    restingHeartRates.add(null);
                } else {
                    Integer rhr = heartValueLog.getRestingHeartRate();
                    restingHeartRates.add(rhr);
                }
            }
        }

        return restingHeartRates;
    }

    public List<Integer> getRestingHeartRate(List<FitbitActivitiesHeartLog> heartLogs) {
        List<Integer> restingHeartRates = new ArrayList<>();

        if (heartLogs == null || heartLogs.isEmpty()) {
            return restingHeartRates;
        }

        for (int i = 0; i < heartLogs.size(); i++) {
            FitbitActivitiesHeartLog heartLog = heartLogs.get(i);
            if (heartLog == null || heartLog.getValues() == null) continue;

            List<FitbitActivitiesHeartValueLog> valueLogs = heartLog.getValues();
            if (valueLogs == null || valueLogs.isEmpty()) continue;

            for (int j = 0; j < valueLogs.size(); j++) {
                FitbitActivitiesHeartValueLog heartValueLog = valueLogs.get(j);
                if (heartValueLog == null) continue;
                Integer rhr = heartValueLog.getRestingHeartRate();
                if (rhr != null) {
                    restingHeartRates.add(rhr);
                }
            }
        }

        return restingHeartRates;
    }

    public List<FitbitActivitySummariesMetricDto> getActivityMetric(List<FitbitActivitySummariesLog> activitySummariesLogs) {
        List<FitbitActivitySummariesMetricDto> metric = new ArrayList<>();
        if (activitySummariesLogs == null || activitySummariesLogs.isEmpty()) {
            return metric;
        }

        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
            if (log == null) continue;
            metric.add(FitbitActivitySummariesMetricDto.builder()
                    .caloriesOut(log.getCaloriesOut())
                    .steps(log.getSteps())
                    .sedentaryMinutes(log.getSedentaryMinutes())
                    .build()
            );
        }

        return metric;
    }

    public List<FitbitActivityGoalsLogMetricDto> getGoalsMetrics(List<FitbitActivityGoalsLog> activityGoalsLogs) {
        List<FitbitActivityGoalsLogMetricDto> activityGoalsLogMetric = new ArrayList<>();
        if (activityGoalsLogs == null || activityGoalsLogs.isEmpty()) {
            return activityGoalsLogMetric;
        }

        for (int i = 0; i < activityGoalsLogs.size(); i++) {
            FitbitActivityGoalsLog log = activityGoalsLogs.get(i);
            if (log == null || log.getActiveMinutes() == null) continue;
            activityGoalsLogMetric.add(FitbitActivityGoalsLogMetricDto.builder()
                    .activeMinutes(log.getActiveMinutes())
                    .build()
            );
        }

        return activityGoalsLogMetric;
    }

    public List<FitbitSleepLogMetricDto> getSleepLogMetric(List<FitbitSleepLog> sleepLogs) {
        List<FitbitSleepLogMetricDto> sleepLogMetrics = new ArrayList<>();
        if (sleepLogs == null || sleepLogs.isEmpty()) {
            return sleepLogMetrics;
        }

        for (int i = 0; i < sleepLogs.size(); i++) {
            FitbitSleepLog log = sleepLogs.get(i);
            if (log == null || log.getEfficiency() == null) continue;
            sleepLogMetrics.add(FitbitSleepLogMetricDto.builder()
                    .efficiency(log.getEfficiency())
                    .build()
            );
        }

        return sleepLogMetrics;
    }

    public List<FitbitSleepSummaryLogMetricDto> getSleepSummaryMetrics(List<FitbitSleepSummaryLog> sleepSummaryLogs) {
        List<FitbitSleepSummaryLogMetricDto> sleepSummaries = new ArrayList<>();
        if (sleepSummaryLogs == null || sleepSummaryLogs.isEmpty()) {
            return sleepSummaries;
        }

        for (int i = 0; i < sleepSummaryLogs.size(); i++) {
            FitbitSleepSummaryLog log = sleepSummaryLogs.get(i);
            if (log == null || log.getTotalMinutesAsleep() == null) continue;
            sleepSummaries.add(FitbitSleepSummaryLogMetricDto.builder()
                    .totalMinutesAsleep(log.getTotalMinutesAsleep())
                    .build()
            );
        }

        return sleepSummaries;
    }

    //Helper functions to calculate Fitbit metrics:

    public int calculateAverageRestingHeartRate(List<Integer> restingHeartRates) {
        if (restingHeartRates == null || restingHeartRates.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;
        for (int i = 0; i < restingHeartRates.size(); i++) {
            if (restingHeartRates.get(i) == null) continue;
            sum = sum + restingHeartRates.get(i);
            count++;
        }

        if (count == 0) {
            return -1;
        }

        return sum / count;
    }

    public int calculateAverageCaloriesOut(List<FitbitActivitySummariesMetricDto> activityMetrics) {
        if (activityMetrics == null || activityMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < activityMetrics.size(); i++) {
            FitbitActivitySummariesMetricDto metric = activityMetrics.get(i);
            if (metric == null || metric.getCaloriesOut() == null) continue;
            sum = sum + metric.getCaloriesOut();
            count++;
        }

        if (count == 0) return -1;

        return sum / count;
    }

    public int calculateAverageSteps(List<FitbitActivitySummariesMetricDto> activityMetrics) {
        if (activityMetrics == null || activityMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < activityMetrics.size(); i++) {
            FitbitActivitySummariesMetricDto metric = activityMetrics.get(i);
            if (metric == null || metric.getSteps() == null) continue;
            sum = sum + metric.getSteps();
            count++;
        }

        if (count == 0) return -1;

        return sum / count;
    }

    public int calculateAverageSedentaryMinutes(List<FitbitActivitySummariesMetricDto> activityMetrics) {
        if (activityMetrics == null || activityMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < activityMetrics.size(); i++) {
            FitbitActivitySummariesMetricDto metric = activityMetrics.get(i);
            if (metric == null || metric.getSedentaryMinutes() == null) continue;
            sum = sum + metric.getSedentaryMinutes();
            count++;
        }

        if (count == 0) return -1;

        return sum / count;
    }

    public int calculateAverageActiveMinutes(List<FitbitActivityGoalsLogMetricDto> activityGoalsLogMetrics) {
        if (activityGoalsLogMetrics == null || activityGoalsLogMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < activityGoalsLogMetrics.size(); i++) {
            FitbitActivityGoalsLogMetricDto metricDto = activityGoalsLogMetrics.get(i);
            if (metricDto == null || metricDto.getActiveMinutes() == null) continue;
            sum = sum + metricDto.getActiveMinutes();
            count++;
        }

        if (count == 0) return -1;
        return sum / count;
    }

    public int calculateAverageEfficiency(List<FitbitSleepLogMetricDto> sleepLogMetrics) {
        if (sleepLogMetrics == null || sleepLogMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < sleepLogMetrics.size(); i++) {
            FitbitSleepLogMetricDto metric = sleepLogMetrics.get(i);
            if (metric == null || metric.getEfficiency() == null) continue;
            sum = sum + metric.getEfficiency();
            count++;
        }

        if (count == 0) return -1;

        return sum / count;
    }

    public int calculateAverageTotalMinutesAsleep(List<FitbitSleepSummaryLogMetricDto> sleepSummaryMetrics) {
        if (sleepSummaryMetrics == null || sleepSummaryMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < sleepSummaryMetrics.size(); i++) {
            FitbitSleepSummaryLogMetricDto metric = sleepSummaryMetrics.get(i);
            if (metric == null || metric.getTotalMinutesAsleep() == null) continue;
            sum = sum + metric.getTotalMinutesAsleep();
            count++;
        }

        if (count == 0) return -1;
        return sum / count;
    }

    //Helper functions to calculate average of Manual log:
    public int calculateAveragePainLevel(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.pain(log.getPainLevel());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageSittingTime(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.sittingTime(log.getSittingTime());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageStandingTime(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.standingTime(log.getStandingTime());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageMorningStiffness(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.morningStiffness(log.getMorningStiffness());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageStressLevel(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.stressLevel(log.getStressLevel());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateDaysWithStretching(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysWithStretching = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog metric = manualDailyLogs.get(i);
            if (metric == null || metric.getStretchingDone() == null) continue;
            if (Boolean.TRUE.equals(metric.getStretchingDone())) {
                daysWithStretching++;
            }
        }

        return daysWithStretching;
    }

    public int calculateDaysWithFlareUp(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysWithFlareUps = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog metric = manualDailyLogs.get(i);
            if (metric == null || metric.getFlareUpToday() == null) continue;
            if (Boolean.TRUE.equals(metric.getFlareUpToday())) {
                daysWithFlareUps++;
            }
        }

        return daysWithFlareUps;
    }

    public int calculateDaysWithNumbnessTingling(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysWithNumbnessTingling = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog metric = manualDailyLogs.get(i);
            if (metric == null || metric.getNumbnessTingling() == null) continue;
            if (Boolean.TRUE.equals(metric.getNumbnessTingling())) {
                daysWithNumbnessTingling++;
            }
        }

        return daysWithNumbnessTingling;
    }

    public int calculateDaysWithLiftingOrStrain(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysWithLiftingOrStrain = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog metric = manualDailyLogs.get(i);
            if (metric == null || metric.getLiftingOrStrain() == null) continue;
            if (Boolean.TRUE.equals(metric.getLiftingOrStrain())) {
                daysWithLiftingOrStrain++;
            }
        }

        return daysWithLiftingOrStrain;
    }

    //Calculate the windowDays:
    public int calculateDaysAvailable(
            List<ManualDailyLog> manualDailyLogs,
            List<FitbitActivitiesHeartLog> heartLogs,
            List<FitbitActivitySummariesLog> activitySummariesLogs,
            List<FitbitActivityGoalsLog> activityGoalsLogs,
            List<FitbitSleepLog> sleepLogs,
            List<FitbitSleepSummaryLog> sleepSummaryLogs
    ) {
        Set<LocalDate> uniqueDates = new HashSet<>();

        if (manualDailyLogs != null && !manualDailyLogs.isEmpty()) {
            for (int i = 0; i < manualDailyLogs.size(); i++) {
                ManualDailyLog log = manualDailyLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (heartLogs != null && !heartLogs.isEmpty()) {
            for (int i = 0; i < heartLogs.size(); i++) {
                FitbitActivitiesHeartLog log = heartLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (activitySummariesLogs != null && !activitySummariesLogs.isEmpty()) {
            for (int i = 0; i < activitySummariesLogs.size(); i++) {
                FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (activityGoalsLogs != null && !activityGoalsLogs.isEmpty()) {
            for (int i = 0; i < activityGoalsLogs.size(); i++) {
                FitbitActivityGoalsLog log = activityGoalsLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (sleepLogs != null && !sleepLogs.isEmpty()) {
            for (int i = 0; i < sleepLogs.size(); i++) {
                FitbitSleepLog log = sleepLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (sleepSummaryLogs != null && !sleepSummaryLogs.isEmpty()) {
            for (int i = 0; i < sleepSummaryLogs.size(); i++) {
                FitbitSleepSummaryLog log = sleepSummaryLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        return uniqueDates.size();
    }

    //Risk forecast:
    public int getYesterdaysSleep(List<FitbitSleepSummaryLog> sleepSummaryLogs) {
        if (sleepSummaryLogs == null || sleepSummaryLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        sleepSummaryLogs.sort(Comparator.comparing(FitbitSleepSummaryLog::getLogDate));

        FitbitSleepSummaryLog log = sleepSummaryLogs.get(sleepSummaryLogs.size() - 1);

        if (log == null || log.getTotalMinutesAsleep() == null) {
            return -1;
        }

        return log.getTotalMinutesAsleep();
    }

    public int getYesterdaysRestingHeartRate(List<FitbitActivitiesHeartLog> heartLogs) {
        if (heartLogs == null || heartLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        heartLogs.sort(Comparator.comparing(FitbitActivitiesHeartLog::getLogDate));

        FitbitActivitiesHeartLog log = heartLogs.get(heartLogs.size() - 1);

        if (log == null || log.getValues() == null || log.getValues().isEmpty()) {
            return -1;
        }

        List<FitbitActivitiesHeartValueLog> values = log.getValues();
        FitbitActivitiesHeartValueLog restingHeartRateLog = values.get(values.size() - 1);

        if (restingHeartRateLog == null || restingHeartRateLog.getRestingHeartRate() == null) {
            return -1;
        }

        return restingHeartRateLog.getRestingHeartRate();
    }

    public int getYesterdaysPainLevel(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        manualDailyLogs.sort(Comparator.comparing(ManualDailyLog::getLogDate));

        ManualDailyLog log = manualDailyLogs.get(manualDailyLogs.size() - 1);

        if (log == null || log.getPainLevel() == null) {
            return -1;
        }

        return EnumScoreHelper.pain(log.getPainLevel());
    }

    public int calculateDaysSinceLastFlareUp(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysSinceLastFlareUps = 0;

        //Sort the list in ascending order of logDate:
        manualDailyLogs.sort(Comparator.comparing(ManualDailyLog::getLogDate));

        for (int i = manualDailyLogs.size() - 1; i >= 0; i--) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null || log.getFlareUpToday() == null) continue;
            if (Boolean.TRUE.equals(log.getFlareUpToday())) {
                return daysSinceLastFlareUps;
            } else {
                daysSinceLastFlareUps++;
            }
        }

        return daysSinceLastFlareUps;
    }

    //Standard Deviations:
    public int calculateStepsStandardDeviation(List<FitbitActivitySummariesLog> activitySummariesLogs) {
        if (activitySummariesLogs == null || activitySummariesLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        //Calculate mean:
        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
            if (log == null || log.getSteps() == null) continue;
            sum = sum + log.getSteps();
            count++;
        }

        if (count == 0) return -1;

        double mean = (double) sum / count;
        double varianceSum = 0.0;
        int validCount = 0;

        //Calculate variance (average of squared differences from the mean):
        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
            if (log == null || log.getSteps() == null) continue;
            double diff = log.getSteps() - mean;
            varianceSum += diff * diff;
            validCount++;
        }

        if (validCount == 0) return -1;

        //Compute variance and standard deviation:
        double variance = varianceSum / validCount;
        return (int) Math.round(Math.sqrt(variance));
    }

    public int calculateRestingHeartRateStandardDeviation(List<FitbitActivitiesHeartLog> heartLogs) {
        if (heartLogs == null || heartLogs.isEmpty()) {
            return -1;
        }

        List<Integer> restingHeartRateArray = new ArrayList<>();

        //Add resting heart rate in to the array:
        for (int i = 0; i < heartLogs.size(); i++) {
            FitbitActivitiesHeartLog log = heartLogs.get(i);
            if (log == null || log.getValues() == null || log.getValues().isEmpty()) continue;
            List<FitbitActivitiesHeartValueLog> values = log.getValues();
            for (int j = 0; j < values.size(); j++) {
                FitbitActivitiesHeartValueLog heartValueLog = values.get(j);
                if (heartValueLog == null || heartValueLog.getRestingHeartRate() == null) continue;
                restingHeartRateArray.add(heartValueLog.getRestingHeartRate());
            }
        }

        if (restingHeartRateArray.isEmpty()) return -1;

        int sum = 0;
        //Calculate mean (average):
        for (int i = 0; i < restingHeartRateArray.size(); i++) {
            sum = sum + restingHeartRateArray.get(i);
        }

        double mean = (double) sum / restingHeartRateArray.size();

        //Calculate variance:
        double varianceSum = 0.0;
        for (int i = 0; i < restingHeartRateArray.size(); i++) {
            double diff = restingHeartRateArray.get(i) - mean;
            varianceSum += diff * diff;
        }

        double variance = varianceSum / restingHeartRateArray.size();

        //Calculate standard deviation (sqrt of variance):
        double stdDev = Math.sqrt(variance);

        //Return rounded integer
        return (int) Math.round(stdDev);
    }

    public int calculateSleepStandardDeviation(List<FitbitSleepSummaryLog> sleepSummaryLogs) {
        if (sleepSummaryLogs == null || sleepSummaryLogs.isEmpty()) {
            return -1;
        }

        List<Integer> sleepMinutesArray = new ArrayList<>();

        for (int i = 0; i < sleepSummaryLogs.size(); i++) {
            FitbitSleepSummaryLog log = sleepSummaryLogs.get(i);
            if (log == null || log.getTotalMinutesAsleep() == null) continue;
            sleepMinutesArray.add(log.getTotalMinutesAsleep());
        }

        if (sleepMinutesArray.isEmpty()) return -1;

        //Calculate mean (average):
        int sum = 0;
        for (int i = 0; i < sleepMinutesArray.size(); i++) {
            sum = sum + sleepMinutesArray.get(i);
        }

        double mean = (double) sum / sleepMinutesArray.size();

        //Calculate variance:
        double varianceSum = 0.0;
        for (int i = 0; i < sleepMinutesArray.size(); i++) {
            double diff = sleepMinutesArray.get(i) - mean;
            varianceSum += diff * diff;
        }

        double variance = varianceSum / sleepMinutesArray.size();

        //Calculate standard deviation:
        double stdDev = Math.sqrt(variance);

        return (int) Math.round(stdDev);
    }

    public int calculateSedentaryStandardDeviation(List<FitbitActivitySummariesLog> activitySummariesLogs) {
        if (activitySummariesLogs == null || activitySummariesLogs.isEmpty()) {
            return -1;
        }

        List<Integer> sedentaryMinutesArray = new ArrayList<>();

        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
            if (log == null || log.getSedentaryMinutes() == null) continue;
            sedentaryMinutesArray.add(log.getSedentaryMinutes());
        }

        //Calculate mean (average):
        int sum = 0;
        for (int i = 0; i < sedentaryMinutesArray.size(); i++) {
            sum = sum + sedentaryMinutesArray.get(i);
        }

        double mean = (double) sum / sedentaryMinutesArray.size();

        //Calculate variance:
        double varianceSum = 0.0;
        for (int i = 0; i < sedentaryMinutesArray.size(); i++) {
            double diff = sedentaryMinutesArray.get(i) - mean;
            varianceSum += diff * diff;
        }
        double variance = varianceSum / sedentaryMinutesArray.size();

        //Calculate standard deviation:
        double stdDev = Math.sqrt(variance);

        return (int) Math.round(stdDev);
    }
}
