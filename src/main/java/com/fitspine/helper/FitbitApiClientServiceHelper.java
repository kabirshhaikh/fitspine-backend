package com.fitspine.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitspine.model.*;
import jakarta.persistence.Column;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class FitbitApiClientServiceHelper {
    public boolean checkForUpdateOfActivitiesLog(FitbitActivitiesLog existing, JsonNode currentActivity) {
        boolean changed = false;

        //To check:
        //description
        String newDescription = currentActivity.has("description") ? currentActivity.get("description").asText() : null;
        if (!Objects.equals(existing.getDescription(), newDescription)) {
            existing.setDescription(newDescription);
            changed = true;
        }

        //calories
        Integer newCalories = currentActivity.has("calories") ? currentActivity.get("calories").asInt() : null;
        if (!Objects.equals(existing.getCalories(), newCalories)) {
            existing.setCalories(newCalories);
            changed = true;
        }

        //distance
        Double newDistance = currentActivity.has("distance") ? currentActivity.get("distance").asDouble() : null;
        if (!Objects.equals(existing.getDistance(), newDistance)) {
            existing.setDistance(newDistance);
            changed = true;
        }

        //  steps
        Integer newSteps = currentActivity.has("steps") ? currentActivity.get("steps").asInt() : null;
        if (!Objects.equals(existing.getSteps(), newSteps)) {
            existing.setSteps(newSteps);
            changed = true;
        }

        //  duration
        Long newDuration = currentActivity.has("duration") ? currentActivity.get("duration").asLong() : null;
        if (!Objects.equals(existing.getDuration(), newDuration)) {
            existing.setDuration(newDuration);
            changed = true;
        }

        //  lastModified
        if (currentActivity.has("lastModified")) {
            String lmString = currentActivity.get("lastModified").asText();
            LocalDateTime newLm = OffsetDateTime.parse(lmString).toLocalDateTime();

            if (!Objects.equals(existing.getLastModified(), newLm)) {
                existing.setLastModified(newLm);
                changed = true;
            }
        }

        //  isFavorite
        Boolean newFavourite = currentActivity.has("isFavorite") ? currentActivity.get("isFavorite").asBoolean() : null;
        if (!Objects.equals(existing.getFavourite(), newFavourite)) {
            existing.setFavourite(newFavourite);
            changed = true;
        }

        //  hasActiveZoneMinutes
        Boolean newHasActiveZoneMinutes = currentActivity.has("hasActiveZoneMinutes") ? currentActivity.get("hasActiveZoneMinutes").asBoolean() : null;
        if (!Objects.equals(existing.getHasActiveZoneMinutes(), newHasActiveZoneMinutes)) {
            existing.setHasActiveZoneMinutes(newHasActiveZoneMinutes);
            changed = true;
        }

        //  hasStartTime
        Boolean newHasStartTime = currentActivity.has("hasStartTime") ? currentActivity.get("hasStartTime").asBoolean() : null;
        if (!Objects.equals(existing.getHasStartTime(), newHasStartTime)) {
            existing.setHasStartTime(newHasStartTime);
            changed = true;
        }

        return changed;
    }


    public boolean checkForUpdateOfActivitiesGoalsLog(FitbitActivityGoalsLog existing, JsonNode currentGoal) {
        boolean changed = false;

        //To check:
        // calories_out
        Integer newCaloriesOut = currentGoal.has("caloriesOut") ? currentGoal.get("caloriesOut").asInt() : null;
        if (!Objects.equals(existing.getCaloriesOut(), newCaloriesOut)) {
            existing.setCaloriesOut(newCaloriesOut);
            changed = true;
        }

        // steps
        Integer newSteps = currentGoal.has("steps") ? currentGoal.get("steps").asInt() : null;
        if (!Objects.equals(existing.getSteps(), newSteps)) {
            existing.setSteps(newSteps);
            changed = true;
        }

        // distance
        Double newDistance = currentGoal.has("distance") ? currentGoal.get("distance").asDouble() : null;
        if (!Objects.equals(existing.getDistance(), newDistance)) {
            existing.setDistance(newDistance);
            changed = true;
        }

        //  floors
        Integer newFloors = currentGoal.has("floors") ? currentGoal.get("floors").asInt() : null;
        if (!Objects.equals(existing.getFloors(), newFloors)) {
            existing.setFloors(newFloors);
            changed = true;
        }

        //  active_minutes
        Integer newActiveMinutes = currentGoal.has("activeMinutes") ? currentGoal.get("activeMinutes").asInt() : null;
        if (!Objects.equals(existing.getActiveMinutes(), newActiveMinutes)) {
            existing.setActiveMinutes(newActiveMinutes);
            changed = true;
        }

        return changed;
    }

    public boolean checkForUpdateOfActivitySummaryLog(FitbitActivitySummariesLog existing, JsonNode currentSummaryLog) {
        boolean changed = false;

        //To check:

        //caloriesOut
        Integer newCaloriesOut = currentSummaryLog.has("caloriesOut") ? currentSummaryLog.get("caloriesOut").asInt() : null;
        if (!Objects.equals(existing.getCaloriesOut(), newCaloriesOut)) {
            existing.setCaloriesOut(newCaloriesOut);
            changed = true;
        }

        //  activityCalories
        Integer newActivityCalories = currentSummaryLog.has("activityCalories") ? currentSummaryLog.get("activityCalories").asInt() : null;
        if (!Objects.equals(existing.getActivityCalories(), newActivityCalories)) {
            existing.setActivityCalories(newActivityCalories);
            changed = true;
        }

        //   caloriesBMR
        Integer newCaloriesBmr = currentSummaryLog.has("caloriesBMR") ? currentSummaryLog.get("caloriesBMR").asInt() : null;
        if (!Objects.equals(existing.getCaloriesBmr(), newCaloriesBmr)) {
            existing.setCaloriesBmr(newCaloriesBmr);
            changed = true;
        }

        //   activeScore
        Integer newActiveScore = currentSummaryLog.has("activeScore") ? currentSummaryLog.get("activeScore").asInt() : null;
        if (!Objects.equals(existing.getActiveScore(), newActiveScore)) {
            existing.setActiveScore(newActiveScore);
            changed = true;
        }

        //   steps
        Integer newSteps = currentSummaryLog.has("steps") ? currentSummaryLog.get("steps").asInt() : null;
        if (!Objects.equals(existing.getSteps(), newSteps)) {
            existing.setSteps(newSteps);
            changed = true;
        }
        //   sedentaryMinutes
        Integer newSedentaryMinutes = currentSummaryLog.has("sedentaryMinutes") ? currentSummaryLog.get("sedentaryMinutes").asInt() : null;
        if (!Objects.equals(existing.getSedentaryMinutes(), newSedentaryMinutes)) {
            existing.setSedentaryMinutes(newSedentaryMinutes);
            changed = true;
        }

        //   lightlyActiveMinutes
        Integer newLightlyActiveMinutes = currentSummaryLog.has("lightlyActiveMinutes") ? currentSummaryLog.get("lightlyActiveMinutes").asInt() : null;
        if (!Objects.equals(existing.getLightlyActiveMinutes(), newLightlyActiveMinutes)) {
            existing.setLightlyActiveMinutes(newLightlyActiveMinutes);
            changed = true;
        }

        //   fairlyActiveMinutes
        Integer newFairlyActiveMinutes = currentSummaryLog.has("fairlyActiveMinutes") ? currentSummaryLog.get("fairlyActiveMinutes").asInt() : null;
        if (!Objects.equals(existing.getFairlyActiveMinutes(), newFairlyActiveMinutes)) {
            existing.setFairlyActiveMinutes(newFairlyActiveMinutes);
            changed = true;
        }

        //   veryActiveMinutes
        Integer newVeryActiveMinutes = currentSummaryLog.has("veryActiveMinutes") ? currentSummaryLog.get("veryActiveMinutes").asInt() : null;
        if (!Objects.equals(existing.getVeryActiveMinutes(), newVeryActiveMinutes)) {
            existing.setVeryActiveMinutes(newVeryActiveMinutes);
            changed = true;
        }

        // marginalCalories
        Integer newMarginalCalories = currentSummaryLog.has("marginalCalories") ? currentSummaryLog.get("marginalCalories").asInt() : null;
        if (!Objects.equals(existing.getMarginalCalories(), newMarginalCalories)) {
            existing.setMarginalCalories(newMarginalCalories);
            changed = true;
        }

        return changed;
    }

    public boolean checkForUpdateOfActivitiesHeartLogAndHeartValueLog(FitbitActivitiesHeartLog existingHeartLog, FitbitActivitiesHeartValueLog existingHeartValueLog, JsonNode currentHeartEntry, JsonNode currentValueArray) {
        boolean changed = false;

        Integer newRestingHeartRate = currentValueArray.has("restingHeartRate") ? currentValueArray.get("restingHeartRate").asInt() : null;

        if (!Objects.equals(existingHeartValueLog.getRestingHeartRate(), newRestingHeartRate)) {
            existingHeartValueLog.setRestingHeartRate(newRestingHeartRate);
            changed = true;
        }

        return changed;
    }

}
