package com.fitspine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitspine.client.FitbitApiClient;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.FitbitApiClientServiceHelper;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.impl.FitbitServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class FitbitApiClientService {
    @Value("${FITBIT_CLIENT_ID}")
    private String clientId;

    @Value("${FITBIT_CLIENT_SECRET}")
    private String clientSecret;

    private final FitbitApiClient fitbitApiClient;
    private final UserRepository userRepository;
    private FitbitServiceImpl fitbitService;

    private final FitbitActivitiesHeartLogRepository fitbitActivitiesHeartLogRepository;

    private final FitbitActivitiesHeartValueLogRepository fitbitActivitiesHeartValueLogRepository;

    private final FitbitActivitiesHeartValueHeartRateZonesLogRepository fitbitActivitiesHeartValueHeartRateZonesLogRepository;
    private final FitbitActivitiesLogRepository fitbitActivitiesLogRepository;
    private final FitbitActivitySummariesLogRepository fitbitActivitySummariesLogRepository;
    private final FitbitActivitySummariesDistancesLogRepository fitbitActivitySummariesDistancesLogRepository;
    private final FitbitActivityGoalsLogRepository fitbitActivityGoalsLogRepository;

    private final FitbitSleepSummaryLogRepository fitbitSleepSummaryLogRepository;
    private final FitbitSleepSummaryStagesLogRepository fitbitSleepSummaryStagesLogRepository;
    private final FitbitSleepLogRepository fitbitSleepLogRepository;
    private final FitbitSleepDataLogRepository fitbitSleepDataLogRepository;
    private final FitbitSleepShortDataLogRepository fitbitSleepShortDataLogRepository;
    private final FitbitApiClientServiceHelper clientServiceHelper;

    public FitbitApiClientService(FitbitApiClient fitbitApiClient,
                                  UserRepository userRepository,
                                  FitbitActivitiesHeartLogRepository fitbitActivitiesHeartLogRepository,
                                  FitbitActivitiesHeartValueLogRepository fitbitActivitiesHeartValueLogRepository,
                                  FitbitActivitiesHeartValueHeartRateZonesLogRepository fitbitActivitiesHeartValueHeartRateZonesLogRepository,
                                  FitbitActivitiesLogRepository fitbitActivitiesLogRepository,
                                  FitbitActivitySummariesLogRepository fitbitActivitySummariesLogRepository,
                                  FitbitActivitySummariesDistancesLogRepository fitbitActivitySummariesDistancesLogRepository,
                                  FitbitActivityGoalsLogRepository fitbitActivityGoalsLogRepository,
                                  FitbitSleepSummaryLogRepository fitbitSleepSummaryLogRepository,
                                  FitbitSleepSummaryStagesLogRepository fitbitSleepSummaryStagesLogRepository,
                                  FitbitSleepLogRepository fitbitSleepLogRepository,
                                  FitbitSleepDataLogRepository fitbitSleepDataLogRepository,
                                  FitbitSleepShortDataLogRepository fitbitSleepShortDataLogRepository,
                                  FitbitServiceImpl fitbitService,
                                  FitbitApiClientServiceHelper clientServiceHelper
    ) {
        this.fitbitApiClient = fitbitApiClient;
        this.userRepository = userRepository;
        this.fitbitActivitiesHeartLogRepository = fitbitActivitiesHeartLogRepository;
        this.fitbitActivitiesHeartValueLogRepository = fitbitActivitiesHeartValueLogRepository;
        this.fitbitActivitiesHeartValueHeartRateZonesLogRepository = fitbitActivitiesHeartValueHeartRateZonesLogRepository;
        this.fitbitActivitiesLogRepository = fitbitActivitiesLogRepository;
        this.fitbitActivitySummariesLogRepository = fitbitActivitySummariesLogRepository;
        this.fitbitActivitySummariesDistancesLogRepository = fitbitActivitySummariesDistancesLogRepository;
        this.fitbitActivityGoalsLogRepository = fitbitActivityGoalsLogRepository;
        this.fitbitSleepSummaryLogRepository = fitbitSleepSummaryLogRepository;
        this.fitbitSleepSummaryStagesLogRepository = fitbitSleepSummaryStagesLogRepository;
        this.fitbitSleepLogRepository = fitbitSleepLogRepository;
        this.fitbitSleepDataLogRepository = fitbitSleepDataLogRepository;
        this.fitbitSleepShortDataLogRepository = fitbitSleepShortDataLogRepository;
        this.fitbitService = fitbitService;
        this.clientServiceHelper = clientServiceHelper;
    }

    @Transactional
    public JsonNode getActivity(String email, String date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
        log.info("Fetching Fitbit steps for user {} on date {}", user.getId(), date);
        JsonNode root = fitbitApiClient.getActivity(user.getId(), clientId, clientSecret, date);

        JsonNode activities = root.get("activities");
        JsonNode summary = root.get("summary");
        JsonNode goals = root.get("goals");

        if ((activities == null || activities.isEmpty()) && (summary == null || summary.isEmpty()) && (goals == null || goals.isEmpty())) {
            log.info("No activities data found for user public ID: {} on date {}: ", user.getPublicId(), date);
            return root;
        }

        LocalDate logDate = LocalDate.parse(date);

        //Save Activities:
        if (activities != null && activities.isArray()) {
            for (JsonNode activity : activities) {
                Long logId = activity.has("logId") ? activity.get("logId").asLong() : null;

                if (logId != null && fitbitActivitiesLogRepository.existsByUserAndLogId(user, logId)) {
                    //I should compare here for update in this block, rest remains same:
                    //First get the old logId object:
                    FitbitActivitiesLog existing = fitbitActivitiesLogRepository.findByUserAndLogId(user, logId).orElse(null);

                    //If existing is not null then compare and update:
                    if (existing != null) {
                        boolean updated = clientServiceHelper.checkForUpdateOfActivitiesLog(existing, activity);

                        if (updated) {
                            fitbitActivitiesLogRepository.save(existing);
                            log.info("Updated activity log {} for user {}", logId, user.getPublicId());
                        } else {
                            log.info("No changes for activity log {} for user {}", logId, user.getPublicId());
                            continue;
                        }
                    }
                }

                FitbitActivitiesLog activitiesLog = new FitbitActivitiesLog();
                activitiesLog.setUser(user);
                activitiesLog.setProvider(fitbitService.getProvider());
                activitiesLog.setLogDate(logDate);
                activitiesLog.setLogId(logId);
                activitiesLog.setActivityId(activity.has("activityId") ? activity.get("activityId").asInt() : null);
                activitiesLog.setActivityParentId(activity.has("activityParentId") ? activity.get("activityParentId").asInt() : null);
                activitiesLog.setActivityParentName(activity.has("activityParentName") ? activity.get("activityParentName").asText() : null);
                activitiesLog.setName(activity.has("name") ? activity.get("name").asText() : null);
                activitiesLog.setDescription(activity.has("description") ? activity.get("description").asText() : null);
                activitiesLog.setCalories(activity.has("calories") ? activity.get("calories").asInt() : null);
                activitiesLog.setDistance(activity.has("distance") ? activity.get("distance").asDouble() : null);
                activitiesLog.setSteps(activity.has("steps") ? activity.get("steps").asInt() : null);
                activitiesLog.setDuration(activity.has("duration") ? activity.get("duration").asLong() : null);

                if (activity.has("lastModified")) {
                    OffsetDateTime lastModified = OffsetDateTime.parse
                            (activity.get("lastModified").asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    activitiesLog.setLastModified(lastModified.toLocalDateTime());
                }

                activitiesLog.setStartTime(activity.has("startTime") ? LocalTime.parse(activity.get("startTime").asText()) : null);
                activitiesLog.setFavourite(activity.has("isFavorite") ? activity.get("isFavorite").asBoolean() : null);
                activitiesLog.setHasActiveZoneMinutes(activity.has("hasActiveZoneMinutes") ? activity.get("hasActiveZoneMinutes").asBoolean() : null);
                activitiesLog.setStartDate(activity.has("startDate") ? LocalDate.parse(activity.get("startDate").asText()) : null);
                activitiesLog.setHasStartTime(activity.has("hasStartTime") ? activity.get("hasStartTime").asBoolean() : null);
                fitbitActivitiesLogRepository.save(activitiesLog);
            }
        }

        //Save Goals:
        if (goals != null && goals.isObject()) {

            //Create:
            if (!fitbitActivityGoalsLogRepository.existsByUserAndLogDate(user, logDate)) {
                FitbitActivityGoalsLog goalsLog = new FitbitActivityGoalsLog();
                goalsLog.setUser(user);
                goalsLog.setProvider(fitbitService.getProvider());
                goalsLog.setLogDate(logDate);
                goalsLog.setCaloriesOut(goals.has("caloriesOut") ? goals.get("caloriesOut").asInt() : null);
                goalsLog.setSteps(goals.has("steps") ? goals.get("steps").asInt() : null);
                goalsLog.setDistance(goals.has("distance") ? goals.get("distance").asDouble() : null);
                goalsLog.setFloors(goals.has("floors") ? goals.get("floors").asInt() : null);
                goalsLog.setActiveMinutes(goals.has("activeMinutes") ? goals.get("activeMinutes").asInt() : null);

                fitbitActivityGoalsLogRepository.save(goalsLog);
            } else {
                //Update:
                //Get existing FitbitActivityGoalsLog:
                FitbitActivityGoalsLog existing = fitbitActivityGoalsLogRepository.findByUserAndLogDate(user, logDate).orElse(null);

                if (existing != null) {
                    boolean updated = clientServiceHelper.checkForUpdateOfActivitiesGoalsLog(existing, goals);

                    if (updated) {
                        fitbitActivityGoalsLogRepository.save(existing);
                        log.info("Updated activity goals log for user {}", user.getPublicId());
                    } else {
                        log.info("No changes for goals log for user {}", user.getPublicId());
                    }
                }
            }
        }


        //Save Summary and distances:
        if (summary != null && summary.isObject()) {
            //Create NEW:
            if (!fitbitActivitySummariesLogRepository.existsByUserAndLogDate(user, logDate)) {
                FitbitActivitySummariesLog summariesLog = new FitbitActivitySummariesLog();
                summariesLog.setUser(user);
                summariesLog.setProvider(fitbitService.getProvider());
                summariesLog.setLogDate(logDate);
                summariesLog.setCaloriesOut(summary.has("caloriesOut") ? summary.get("caloriesOut").asInt() : null);
                summariesLog.setActivityCalories(summary.has("activityCalories") ? summary.get("activityCalories").asInt() : null);
                summariesLog.setCaloriesBmr(summary.has("caloriesBMR") ? summary.get("caloriesBMR").asInt() : null);
                summariesLog.setActiveScore(summary.has("activeScore") ? summary.get("activeScore").asInt() : null);
                summariesLog.setSteps(summary.has("steps") ? summary.get("steps").asInt() : null);
                summariesLog.setSedentaryMinutes(summary.has("sedentaryMinutes") ? summary.get("sedentaryMinutes").asInt() : null);
                summariesLog.setLightlyActiveMinutes(summary.has("lightlyActiveMinutes") ? summary.get("lightlyActiveMinutes").asInt() : null);
                summariesLog.setFairlyActiveMinutes(summary.has("fairlyActiveMinutes") ? summary.get("fairlyActiveMinutes").asInt() : null);
                summariesLog.setVeryActiveMinutes(summary.has("veryActiveMinutes") ? summary.get("veryActiveMinutes").asInt() : null);
                summariesLog.setMarginalCalories(summary.has("marginalCalories") ? summary.get("marginalCalories").asInt() : null);
                summariesLog.setRawJson(summary.toString());
                FitbitActivitySummariesLog savedSummary = fitbitActivitySummariesLogRepository.save(summariesLog);

                JsonNode distances = summary.get("distances");
                if (distances != null && distances.isArray()) {
                    for (JsonNode d : distances) {
                        FitbitActivitySummariesDistancesLog distancesLog = new FitbitActivitySummariesDistancesLog();
                        distancesLog.setFitbitActivitySummariesLog(savedSummary);
                        distancesLog.setActivity(d.has("activity") ? d.get("activity").asText() : null);
                        distancesLog.setDistance(d.has("distance") ? d.get("distance").asDouble() : null);
                        fitbitActivitySummariesDistancesLogRepository.save(distancesLog);
                    }
                }
            } else {
                //Update:
                //Get existing FitbitActivitySummariesLog:
                FitbitActivitySummariesLog existing = fitbitActivitySummariesLogRepository.findByUserAndLogDate(user, logDate).orElse(null);

                //Compare and update:
                if (existing != null) {
                    boolean updated = clientServiceHelper.checkForUpdateOfActivitySummaryLog(existing, summary);

                    if (updated) {
                        //Set raw json:
                        existing.setRawJson(summary.toString());

                        fitbitActivitySummariesLogRepository.save(existing);
                        log.info("Updated summaries log for user public ID: {} for date: {}", user.getPublicId(), logDate);

                        //Delete distances:
                        fitbitActivitySummariesDistancesLogRepository.deleteByFitbitActivitySummariesLog(existing);
                        log.info("Deleted summaries distances because the summaries log was updated");

                        //Recreate new distances:
                        JsonNode distances = summary.get("distances");
                        if (distances != null && distances.isArray()) {
                            for (JsonNode d : distances) {
                                FitbitActivitySummariesDistancesLog distancesLog = new FitbitActivitySummariesDistancesLog();
                                distancesLog.setFitbitActivitySummariesLog(existing);
                                distancesLog.setActivity(d.has("activity") ? d.get("activity").asText() : null);
                                distancesLog.setDistance(d.has("distance") ? d.get("distance").asDouble() : null);
                                fitbitActivitySummariesDistancesLogRepository.save(distancesLog);
                                log.info("Recreated new distance for summaries log on update");
                            }
                        }
                    } else {
                        log.info("No summary changes for user {} on {}", user.getPublicId(), logDate);
                    }
                }
            }
        }

        return root;
    }

    @Transactional
    public JsonNode getSleep(String email, String date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
        log.info("Fetching Fitbit sleep for user {} on date {}", user.getId(), date);
        JsonNode root = fitbitApiClient.getSleep(user.getId(), clientId, clientSecret, date);

        JsonNode sleep = root.get("sleep");
        JsonNode summary = root.get("summary");

        if ((sleep == null || sleep.isEmpty()) && (summary == null || summary.isEmpty())) {
            log.warn("No sleep data found for user public ID: {} on date {}: ", user.getPublicId(), date);
            return root;
        }

        //Save sleep:
        if (sleep != null && sleep.isArray()) {
            for (JsonNode s : sleep) {
                Long logId = s.get("logId").asLong();

                if (fitbitSleepLogRepository.existsByUserAndLogId(user, logId)) {
                    //I should compare here for update in this block, rest remains same:
                    log.info("Skipping duplicate sleep log {} for user public ID: {}", logId, user.getPublicId());
                    continue;
                }

                FitbitSleepLog sleepLog = new FitbitSleepLog();
                sleepLog.setUser(user);
                sleepLog.setProvider(fitbitService.getProvider());
                sleepLog.setLogDate(LocalDate.parse(date));
                sleepLog.setDateOfSleep(LocalDate.parse(s.get("dateOfSleep").asText()));
                sleepLog.setEfficiency(s.has("efficiency") ? s.get("efficiency").asInt() : null);
                sleepLog.setStartTime(LocalDateTime.parse(s.get("startTime").asText()));
                sleepLog.setEndTime(LocalDateTime.parse(s.get("endTime").asText()));
                sleepLog.setInfoCode(s.has("infoCode") ? s.get("infoCode").asInt() : null);
                sleepLog.setIsMainSleep(s.has("isMainSleep") && s.get("isMainSleep").asBoolean());
                sleepLog.setLogId(s.has("logId") ? s.get("logId").asLong() : null);
                sleepLog.setMinutesAfterWakeup(s.has("minutesAfterWakeup") ? s.get("minutesAfterWakeup").asInt() : null);
                sleepLog.setMinutesAwake(s.has("minutesAwake") ? s.get("minutesAwake").asInt() : null);
                sleepLog.setMinutesAsleep(s.has("minutesAsleep") ? s.get("minutesAsleep").asInt() : null);
                sleepLog.setMinutesToFallAsleep(s.has("minutesToFallAsleep") ? s.get("minutesToFallAsleep").asInt() : null);
                sleepLog.setLogType(s.has("logType") ? s.get("logType").asText() : null);
                sleepLog.setTimeInBed(s.has("timeInBed") ? s.get("timeInBed").asInt() : null);
                sleepLog.setType(s.has("type") ? s.get("type").asText() : null);
                FitbitSleepLog savedSleepLog = fitbitSleepLogRepository.save(sleepLog);

                //Save sleep data:
                JsonNode levels = s.get("levels");
                if (levels != null && levels.has("data")) {
                    for (JsonNode d : levels.get("data")) {
                        FitbitSleepDataLog dataLog = new FitbitSleepDataLog();
                        dataLog.setFitbitSleepLog(savedSleepLog);
                        dataLog.setDateTime(LocalDateTime.parse(d.get("dateTime").asText()));
                        dataLog.setLevel(d.get("level").asText());
                        dataLog.setSeconds(d.get("seconds").asInt());
                        fitbitSleepDataLogRepository.save(dataLog);
                    }
                }

                //Save sleep short data:
                if (levels != null && levels.has("shortData")) {
                    for (JsonNode sd : levels.get("shortData")) {
                        FitbitSleepShortDataLog shortDataLog = new FitbitSleepShortDataLog();
                        shortDataLog.setFitbitSleepLog(savedSleepLog);
                        shortDataLog.setDateTime(LocalDateTime.parse(sd.get("dateTime").asText()));
                        shortDataLog.setLevel(sd.get("level").asText());
                        shortDataLog.setSeconds(sd.get("seconds").asInt());
                        fitbitSleepShortDataLogRepository.save(shortDataLog);
                    }
                }
            }
        }


        //Save Summary:
        if (summary != null && summary.isObject()) {
            LocalDate logDate = LocalDate.parse(date);

            if (fitbitSleepSummaryLogRepository.existsByUserAndLogDate(user, logDate)) {
                //I should compare here for update in this block, rest remains same:
                log.info("Skipping duplicate sleep summary for user public ID: {} on {}: ", user.getPublicId(), logDate);
                return root;
            }

            FitbitSleepSummaryLog sleepSummaryLog = new FitbitSleepSummaryLog();
            sleepSummaryLog.setUser(user);
            sleepSummaryLog.setProvider(fitbitService.getProvider());
            sleepSummaryLog.setLogDate(LocalDate.parse(date));
            sleepSummaryLog.setTotalMinutesAsleep(summary.has("totalMinutesAsleep") ? summary.get("totalMinutesAsleep").asInt() : null);
            sleepSummaryLog.setTotalSleepRecords(summary.has("totalSleepRecords") ? summary.get("totalSleepRecords").asInt() : null);
            sleepSummaryLog.setTotalTimeInBed(summary.has("totalTimeInBed") ? summary.get("totalTimeInBed").asInt() : null);
            sleepSummaryLog.setRawJson(root.toString());
            FitbitSleepSummaryLog savedSleepSummaryLog = fitbitSleepSummaryLogRepository.save(sleepSummaryLog);

            //Extract stages from summary:
            JsonNode stages = summary.get("stages");

            //Save Stages:
            if (stages != null && stages.isObject()) {
                FitbitSleepSummaryStagesLog stagesLog = new FitbitSleepSummaryStagesLog();
                stagesLog.setFitbitSleepSummaryLog(savedSleepSummaryLog);
                stagesLog.setDeep(stages.has("deep") ? stages.get("deep").asInt() : null);
                stagesLog.setLight(stages.has("light") ? stages.get("light").asInt() : null);
                stagesLog.setRem(stages.has("rem") ? stages.get("rem").asInt() : null);
                stagesLog.setWake(stages.has("wake") ? stages.get("wake").asInt() : null);
                fitbitSleepSummaryStagesLogRepository.save(stagesLog);
            }
        }

        return root;
    }

    @Transactional
    public JsonNode getHeartRate(String email, String date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
        log.info("Fetching Fitbit heart rate for user {} on date {}", user.getId(), date);
        JsonNode root = fitbitApiClient.getHeartRate(user.getId(), clientId, clientSecret, date);

        JsonNode activitiesHeartArray = root.get("activities-heart");
        if (activitiesHeartArray == null || !activitiesHeartArray.isArray() || activitiesHeartArray.isEmpty()) {
            log.warn("No heart rate data found for user {} on date {}", user.getId(), date);
            return root;
        }

        JsonNode heartEntry = activitiesHeartArray.get(0);

        LocalDate logDate = heartEntry.has("dateTime")
                ? LocalDate.parse(heartEntry.get("dateTime").asText())
                : LocalDate.parse(date);

        JsonNode valueArray = heartEntry.get("value");

        Integer restingHeartRate = (valueArray != null && valueArray.has("restingHeartRate"))
                ? valueArray.get("restingHeartRate").asInt()
                : null;

        JsonNode heartRateZonesArray = (valueArray != null) ? valueArray.get("heartRateZones") : null;

        if (fitbitActivitiesHeartLogRepository.existsByUserAndLogDate(user, logDate)) {
            //I should compare here for update in this block, rest remains same:
            log.info("Skipping duplicate heart log for user Public ID: {} on date {}", user.getPublicId(), logDate);
            return root;
        }

        //Save heart log:
        FitbitActivitiesHeartLog activitiesHeartLog = new FitbitActivitiesHeartLog();
        activitiesHeartLog.setUser(user);
        activitiesHeartLog.setLogDate(logDate);
        activitiesHeartLog.setProvider(fitbitService.getProvider());
        activitiesHeartLog.setDateTime(logDate);
        activitiesHeartLog.setRawJson(root.toString());
        FitbitActivitiesHeartLog savedHeartLog = fitbitActivitiesHeartLogRepository.save(activitiesHeartLog);

        //Save value log:
        FitbitActivitiesHeartValueLog valueLog = new FitbitActivitiesHeartValueLog();
        valueLog.setFitbitActivitiesHeartLog(savedHeartLog);
        valueLog.setRestingHeartRate(restingHeartRate);
        FitbitActivitiesHeartValueLog savedValueLog = fitbitActivitiesHeartValueLogRepository.save(valueLog);

        //Save heart zones:
        if (heartRateZonesArray != null && heartRateZonesArray.isArray()) {
            for (JsonNode zone : heartRateZonesArray) {
                FitbitActivitiesHeartValueHeartRateZonesLog zonesLog = new FitbitActivitiesHeartValueHeartRateZonesLog();
                zonesLog.setFitbitActivitiesHeartValuesLog(savedValueLog);
                zonesLog.setName(zone.has("name") ? zone.get("name").asText() : null);
                zonesLog.setMin(zone.has("min") ? zone.get("min").asInt() : null);
                zonesLog.setMax(zone.has("max") ? zone.get("max").asInt() : null);
                zonesLog.setMinutes(zone.has("minutes") ? zone.get("minutes").asInt() : null);
                zonesLog.setCaloriesOut(zone.has("caloriesOut") ? zone.get("caloriesOut").asDouble() : null);
                fitbitActivitiesHeartValueHeartRateZonesLogRepository.save(zonesLog);
            }
        }

        return root;
    }
}
