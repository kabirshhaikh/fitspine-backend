package com.fitspine.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.dto.AiInsightResponseDto;
import com.fitspine.dto.FlareUpTriggersDto;
import com.fitspine.exception.AiServiceException;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class AiInsightHelper {
    private final RestTemplate restTemplate;
    private final AiDailyInsightRepository insightRepository;
    private final AiDailyInsightFlareUpTriggersRepository flareUpTriggersRepository;
    private final AiDailyInsightImprovedRepository improvedRepository;
    private final AiDailyInsightWorsenedRepository worsenedRepository;
    private final AiDailyInsightPossibleCausesRepository possibleCausesRepository;
    private final AiDailyInsightActionableAdviceRepository actionableAdviceRepository;
    private final AiDailyInsightInterventionsTodayRepository interventionsTodayRepository;
    private final AiDailyInsightRiskForecastsRepository riskForecastsRepository;

    private final ObjectMapper objectMapper;

    public AiInsightHelper(
            RestTemplate restTemplate,
            AiDailyInsightRepository insightRepository,
            AiDailyInsightFlareUpTriggersRepository flareUpTriggersRepository,
            AiDailyInsightImprovedRepository improvedRepository,
            AiDailyInsightWorsenedRepository worsenedRepository,
            AiDailyInsightPossibleCausesRepository possibleCausesRepository,
            AiDailyInsightActionableAdviceRepository actionableAdviceRepository,
            AiDailyInsightInterventionsTodayRepository interventionsTodayRepository,
            AiDailyInsightRiskForecastsRepository riskForecastsRepository,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.insightRepository = insightRepository;
        this.flareUpTriggersRepository = flareUpTriggersRepository;
        this.improvedRepository = improvedRepository;
        this.worsenedRepository = worsenedRepository;
        this.possibleCausesRepository = possibleCausesRepository;
        this.actionableAdviceRepository = actionableAdviceRepository;
        this.interventionsTodayRepository = interventionsTodayRepository;
        this.riskForecastsRepository = riskForecastsRepository;
        this.objectMapper = objectMapper;
    }

    public List<FlareUpTriggersDto> returnTriggerText(List<AiDailyInsightFlareUpTriggers> list) {
        List<FlareUpTriggersDto> flare = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            flare.add(FlareUpTriggersDto.builder()
                    .metric(list.get(i).getMetric())
                    .value(list.get(i).getValue())
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

    public String sha256(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public String callOpenAi(String todayJson, String contextJson, String SYSTEM_PROMPT, String apiKey, String OPENAI_URL) {
        //Build request body:
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("temperature", 0.7);

        //Create list of map:
        HashMap<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);

        HashMap<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", String.format("todayJson: %s\ncontextJson: %s", todayJson, contextJson));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);

        body.put("messages", messages);

        //Add headers:
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        //Send the request:
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }

    public void validateStringArray(JsonNode json, String fieldName) {
        JsonNode arr = json.get(fieldName);


        if (arr == null || !arr.isArray()) {
            return;
        }

        if (arr.size() > 0 && !arr.get(0).isTextual()) {
            throw new AiServiceException("Invalid JSON format: '" + fieldName + "' must be an array of strings.");
        }
    }

    public AiInsightResponseDto saveDataForAiInsight(User user, LocalDate logDate, String responseBody) throws Exception {
        var root = objectMapper.readTree(responseBody);
        String modelUsed = root.path("model").asText("unknown");
        int totalTokensUsed = root.path("usage").path("total_tokens").asInt(0);
        int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
        int completionToken = root.path("usage").path("completion_tokens").asInt(0);

        JsonNode choicesNode = root.path("choices");

        if (!choicesNode.isArray() || choicesNode.isEmpty()) {
            throw new AiServiceException("OpenAI response missing 'choices' array");
        }

        JsonNode messageNode = choicesNode.get(0).path("message").path("content");

        String content = messageNode.asText(null);
        if (content == null || content.isBlank()) {
            throw new AiServiceException("OpenAI response missing content");
        }

        content = content
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        JsonNode json = objectMapper.readTree(content);

        //Validate the fields and check if they are array or not:
        validateStringArray(json, "improved");
        validateStringArray(json, "worsened");
        validateStringArray(json, "possibleCauses");
        validateStringArray(json, "actionableAdvice");
        validateStringArray(json, "interventionsToday");

        AiInsightResponseDto insight = objectMapper.readValue(content, AiInsightResponseDto.class);

        //Search existing AI insight to update/override if it exists:
        AiDailyInsight existingInsight = insightRepository.findByUserAndLogDate(user, logDate).orElse(null);
        AiDailyInsight savedInsight;

        //UPDATE:
        if (existingInsight != null) {
            log.info("Updating existing AI Insight for user {} on date {}", user.getPublicId(), logDate);

            //Override the fields with new values:
            existingInsight.setCompletionTokens(completionToken);
            existingInsight.setModelUsed(modelUsed);
            existingInsight.setPromptTokens(promptTokens);
            existingInsight.setTotalTokens(totalTokensUsed);
            existingInsight.setDiscProtectionScore(insight.getDiscProtectionScore());
            existingInsight.setDiscScoreExplanation(insight.getDiscScoreExplanation());
            existingInsight.setRecoveryInsights(insight.getRecoveryInsights());
            existingInsight.setTodaysInsights(insight.getTodaysInsight());

            //Update the parent entity:
            savedInsight = insightRepository.save(existingInsight);

            //Get count of risk forecast before delete:
            int count = riskForecastsRepository.countByAiDailyInsight_UserAndAiDailyInsight_LogDate(user, logDate);

            //Delete children entities after updating parent:
            improvedRepository.deleteByAiDailyInsight(existingInsight);
            worsenedRepository.deleteByAiDailyInsight(existingInsight);
            flareUpTriggersRepository.deleteByAiDailyInsight(existingInsight);

            //To delete a one to one mapping, you need to first set it as null and then perform delete using the repo:
            existingInsight.setRiskForecasts(null);
            riskForecastsRepository.deleteByAiDailyInsight(existingInsight);
            actionableAdviceRepository.deleteByAiDailyInsight(existingInsight);
            interventionsTodayRepository.deleteByAiDailyInsight(existingInsight);
            possibleCausesRepository.deleteByAiDailyInsight(existingInsight);
        }
        //CREATE
        else {
            log.info("Creating new AI insight for user {} on date {}", user.getPublicId(), logDate);

            savedInsight = AiDailyInsight.builder()
                    .user(user)
                    .logDate(logDate)
                    .provider(user.getWearableType() != null ? user.getWearableType() : null)
                    .todaysInsights(insight.getTodaysInsight() != null ? insight.getTodaysInsight() : null)
                    .recoveryInsights(insight.getRecoveryInsights() != null ? insight.getRecoveryInsights() : null)
                    .discScoreExplanation(insight.getDiscScoreExplanation() != null ? insight.getDiscScoreExplanation() : null)
                    .discProtectionScore(insight.getDiscProtectionScore() != null ? insight.getDiscProtectionScore() : null)
                    .modelUsed(modelUsed)
                    .totalTokens(totalTokensUsed)
                    .promptTokens(promptTokens)
                    .completionTokens(completionToken)
                    .build();

            //Save the insight:
            insightRepository.save(savedInsight);
        }

        //Save children's always fresh (Look the block up, I have deleted the children entities after overriding the parent entity):
        log.info("Updating the children entities for AI Insight for user {} on date {}", user.getPublicId(), logDate);

        if (insight.getFlareUpTriggers() != null && !insight.getFlareUpTriggers().isEmpty()) {
            flareUpTriggersRepository.saveAll(getFlareUpTriggers(insight.getFlareUpTriggers(), savedInsight));
        }

        if (insight.getImproved() != null && !insight.getImproved().isEmpty()) {
            improvedRepository.saveAll(getImprovedList(insight.getImproved(), savedInsight));
        }

        if (insight.getWorsened() != null && !insight.getWorsened().isEmpty()) {
            worsenedRepository.saveAll(getWorsened(insight.getWorsened(), savedInsight));
        }

        if (insight.getPossibleCauses() != null && !insight.getPossibleCauses().isEmpty()) {
            possibleCausesRepository.saveAll(getPossibleIssues(insight.getPossibleCauses(), savedInsight));
        }

        if (insight.getActionableAdvice() != null && !insight.getActionableAdvice().isEmpty()) {
            actionableAdviceRepository.saveAll(getActionableAdvice(insight.getActionableAdvice(), savedInsight));
        }

        if (insight.getInterventionsToday() != null && !insight.getInterventionsToday().isEmpty()) {
            interventionsTodayRepository.saveAll(getInterventionsToday(insight.getInterventionsToday(), savedInsight));
        }

        if (insight.getRiskForecast() != null) {
            riskForecastsRepository.save(
                    AiDailyInsightRiskForecasts.builder()
                            .aiDailyInsight(savedInsight)
                            .risk(insight.getRiskForecast().getRisk())
                            .bucket(insight.getRiskForecast().getBucket())
                            .build()
            );
        }

        log.info("Updating the children entities for AI Insight for user {} on date {} finished", user.getPublicId(), logDate);

        return insight;
    }
}
