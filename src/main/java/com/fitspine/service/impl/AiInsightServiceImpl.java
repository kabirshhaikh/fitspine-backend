package com.fitspine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.dto.*;
import com.fitspine.exception.AiServiceException;
import com.fitspine.exception.ResourceNotFoundException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.AiInsightHelper;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.AiInsightService;
import com.fitspine.service.FitbitContextAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiInsightServiceImpl implements AiInsightService {
    @Value("${openai.api.key}")
    private String apiKey;
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AiDailyInsightRepository insightRepository;
    private final AiDailyInsightFlareUpTriggersRepository flareUpTriggersRepository;
    private final UserRepository userRepository;
    private final AiInsightHelper aiHelper;
    private final FitbitContextAggregationService fitbitContextAggregationService;
    private final AiDailyInsightImprovedRepository improvedRepository;
    private final AiDailyInsightWorsenedRepository worsenedRepository;

    private final AiDailyInsightPossibleCausesRepository possibleCausesRepository;
    private final AiDailyInsightActionableAdviceRepository actionableAdviceRepository;
    private final AiDailyInsightInterventionsTodayRepository interventionsTodayRepository;
    private final AiDailyInsightRiskForecastsRepository riskForecastsRepository;


    public AiInsightServiceImpl(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            AiDailyInsightRepository insightRepository,
            UserRepository userRepository,
            AiInsightHelper aiHelper,
            AiDailyInsightFlareUpTriggersRepository flareUpTriggersRepository,
            FitbitContextAggregationService fitbitContextAggregationService,
            AiDailyInsightImprovedRepository improvedRepository,
            AiDailyInsightWorsenedRepository worsenedRepository,
            AiDailyInsightPossibleCausesRepository possibleCausesRepository,
            AiDailyInsightActionableAdviceRepository actionableAdviceRepository,
            AiDailyInsightInterventionsTodayRepository interventionsTodayRepository,
            AiDailyInsightRiskForecastsRepository riskForecastsRepository
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.insightRepository = insightRepository;
        this.flareUpTriggersRepository = flareUpTriggersRepository;
        this.userRepository = userRepository;
        this.aiHelper = aiHelper;
        this.fitbitContextAggregationService = fitbitContextAggregationService;
        this.improvedRepository = improvedRepository;
        this.worsenedRepository = worsenedRepository;
        this.possibleCausesRepository = possibleCausesRepository;
        this.actionableAdviceRepository = actionableAdviceRepository;
        this.interventionsTodayRepository = interventionsTodayRepository;
        this.riskForecastsRepository = riskForecastsRepository;
    }

    private static final String FIELD_CONTEXT_EXTENDED = """
            Field Reference for FitSpine AI

            ---
                        
            General Rules:
            - Any value of **-1** means "no data available" and should be ignored in all comparisons.
            - If both today and context values are -1 → skip metric entirely.
            - Higher numeric values generally indicate worse conditions (pain, stress, stiffness, sitting time, etc.).
            - Lower numeric values indicate better recovery or healthier status (sleep duration, heart rate, activity).

            ---

            Current Day (todayJson → AiUserDailyInputDto)
            - painLevel: User's perceived pain [0=None, 1=Mild, 2=Moderate, 3=Severe]
            - flareUpToday: Whether a flare-up occurred today (true/false)
            - numbnessTingling: Indicates nerve irritation (true/false)
            - sittingTime: Daily sitting duration [0=<2h, 1=2–4h, 2=4–6h, 3=6–8h, 4=>8h]
            - standingTime: Daily standing duration [same scale]
            - stretchingDone: If user performed spine-protective stretching
            - morningStiffness: Stiffness after waking [0=None, 1=Mild, 2=Moderate, 3=Severe]
            - stressLevel: Daily psychological stress [0=Very Low → 4=Very High]
            - liftingOrStrain: True if heavy lifting or awkward posture occurred
            - restingHeartRate: Resting heart rate (bpm)
            - caloriesOut: Total calories burned (BMR + activity)
            - activityCalories: Calories from intentional exercise
            - caloriesBMR: Basal metabolic rate calories
            - steps: Total daily steps
            - sedentaryMinutes: Minutes spent inactive
            - activeMinutes: Sum of lightly, fairly, and very active minutes
            - totalMinutesAsleep: Total minutes asleep
            - efficiency: Sleep quality (0–100)
            - timeInBed: Total minutes in bed (asleep + awake)
            - notes: User’s textual remarks about the day

            ---

            7-Day Context (contextJson → FitbitAiContextInsightDto)
            - windowDays: Number of days in rolling window (usually 7)
            - daysAvailable: How many days had valid data
            - averagePainLevel: Mean of daily pain scores
            - averageSittingTime: Mean of sitting time category
            - averageStandingTime: Mean of standing time category
            - averageMorningStiffness: Mean of stiffness score
            - averageStressLevel: Mean of stress score
            - daysWithStretching: Count of days stretchingDone=true
            - daysWithFlareups: Count of days with flare-ups
            - daysWithNumbnessTingling: Count of days with tingling symptoms
            - daysWithLiftingOrStrain: Count of days with strain
            - averageRestingHeartRate: Mean RHR over window
            - averageCaloriesOut: Mean calories burned
            - averageSteps: Mean daily steps
            - averageSedentaryMinutes: Mean sedentary time
            - averageActiveMinutes: Mean active minutes
            - averageTotalMinutesAsleep: Mean sleep minutes
            - averageEfficiency: Mean sleep efficiency
            - yesterdaySleepMinutes: Sleep from the previous day
            - yesterdayRestingHeartRate: RHR from previous day
            - yesterdayPainLevel: Pain score from yesterday
            - daysSinceLastFlareUp: Days since last flare-up
            - stepsStandardDeviation: Variability in steps
            - restingHearRateStandardDeviation: Variability in RHR
            - sleepStandardDeviation: Variability in sleep
            - sedentaryStandardDeviation: Variability in sedentary time

            ---
            """;

    @Transactional
    @Override
    public AiInsightResponseDto generateDailyInsight(AiUserDailyInputDto dto, String email, LocalDate logDate) {
        //Get the user:
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        //Check if the ai insight exists:
        boolean insightExists = insightRepository.existsByUserAndLogDate(user, logDate);

        //If it exists then map the existing insight to dto and return that:
        if (insightExists) {
            AiDailyInsight insight = insightRepository.findByUserAndLogDate(user, logDate).orElseThrow(() -> new ResourceNotFoundException("Ai daily insight not found for date:" + logDate));

            List<AiDailyInsightFlareUpTriggers> flareUpTriggers = insight.getFlareUpTriggers();
            List<FlareUpTriggersDto> triggers = aiHelper.returnTriggerText(flareUpTriggers);

            List<AiDailyInsightImproved> improvedList = insight.getImproved();
            List<String> improved = aiHelper.returnImprovedList(improvedList);

            List<AiDailyInsightWorsened> worsenedList = insight.getWorsened();
            List<String> worsened = aiHelper.returnWorsenedList(worsenedList);

            List<AiDailyInsightPossibleCauses> possibleCausesList = insight.getPossibleCausesList();
            List<String> possibleCauses = aiHelper.returnPossibleCausesList(possibleCausesList);

            List<String> actionableAdvice = aiHelper.returnActionableAdviceList(insight.getActionableAdvices());

            List<String> interventionsToday = aiHelper.returnInterventionsTodayList(insight.getInterventionsToday());

            RiskForecastDto riskForecastDto = null;
            if (insight.getRiskForecasts() != null) {
                riskForecastDto = RiskForecastDto.builder()
                        .risk(insight.getRiskForecasts().getRisk())
                        .bucket(insight.getRiskForecasts().getBucket())
                        .build();
            }

            return AiInsightResponseDto.builder()
                    .todaysInsight(insight.getTodaysInsights() != null ? insight.getTodaysInsights() : "")
                    .recoveryInsights(insight.getRecoveryInsights() != null ? insight.getRecoveryInsights() : "")
                    .discProtectionScore(insight.getDiscProtectionScore() != null ? insight.getDiscProtectionScore() : 0)
                    .discScoreExplanation(insight.getDiscScoreExplanation() != null ? insight.getDiscScoreExplanation() : "")

                    .flareUpTriggers(triggers != null ? triggers : new ArrayList<>())
                    .improved(improved != null ? improved : new ArrayList<>())
                    .worsened(worsened != null ? worsened : new ArrayList<>())
                    .possibleCauses(possibleCauses != null ? possibleCauses : new ArrayList<>())
                    .actionableAdvice(actionableAdvice != null ? actionableAdvice : new ArrayList<>())
                    .interventionsToday(interventionsToday != null ? interventionsToday : new ArrayList<>())
                    .riskForecast(riskForecastDto)
                    .build();
        }

        FitbitAiContextInsightDto contextDto = fitbitContextAggregationService.buildContext(email, logDate); //Here logDate is he currentDate meaning target date

        try {
            String todayJson = objectMapper.writeValueAsString(dto);
            String contextJson = objectMapper.writeValueAsString(contextDto);

            log.info("Today's json: {}", todayJson);
            log.info("Context json: {}", contextJson);

            //AI prompt:
            String prompt = String.format("""
                    You are **FitSpine AI**, an advanced spine health and recovery intelligence system.

                    Goal:
                    Analyze biomechanical, neurological, and lifestyle data to identify patterns,
                    compare the user's *current day* (todayJson) with *7-day averages* (contextJson),
                    and generate structured, clinically meaningful recovery insights.

                    ---

                    FIELD CONTEXT (definitions, scaling, and interpretation):
                    %s

                    Interpretation Guidelines:
                    - Any value of -1 means no data available — skip that metric.
                    - Compare only metrics present in both JSONs.
                    - Higher numeric = worse symptom/load; Lower numeric = healthier/recovered.
                    - Boolean fields (true/false) represent behavior or symptom presence.

                    ---

                    Comparison Rules for Output Sections:

                    **For "improved":**
                    Mark metrics as improved if today's value is better (lower for pain/stress/stiffness/sitting; higher for sleep/steps).
                    - painLevel ↓, stressLevel ↓, morningStiffness ↓, sittingTime ↓
                    - restingHeartRate ↓
                    - totalMinutesAsleep ↑, efficiency ↑
                    - steps ↑, activeMinutes ↑

                    **For "worsened":**
                    Mark metrics as worsened if today's value is worse.
                    - painLevel ↑, stressLevel ↑, morningStiffness ↑
                    - sittingTime ↑, sedentaryMinutes ↑
                    - totalMinutesAsleep ↓, efficiency ↓
                    - steps ↓, activeMinutes ↓
                    - restingHeartRate ↑

                    **For "possibleCauses":**
                    Detect cause-effect relations:
                    - pain ↑ + sittingTime ↑ → posture-related load
                    - pain ↑ + stress ↑ → stress-linked flare-up
                    - stiffness ↑ + sleep ↓ → poor overnight recovery
                    - numbnessTingling + liftingOrStrain → mechanical irritation
                    - stress ↓ + steps ↑ → positive recovery adaptation

                    **For "actionableAdvice":**
                    Map worsened metrics to behavioral actions:
                    - sittingTime ↑ → "Take short standing breaks every 45–60 min"
                    - stressLevel ↑ → "Try box breathing or light mindfulness"
                    - stiffness ↑ → "Perform McGill Big-3 exercises"
                    - sleep ↓ → "Wind down earlier; reduce screen use before bed"
                    - low steps → "Add short evening walk or gentle mobility"

                    **For "discProtectionScore":**
                    Assign 0–100 based on:
                    + Add points for low pain/stress, stretchingDone=true, good sleep, high steps, RHR↓
                    − Subtract for pain/stress↑, long sitting, poor sleep, RHR↑, flare-up=true
                    Include a short "discScoreExplanation" summarizing why score changed.

                    **For "flareUpTriggers":**
                    Compare today vs averages:
                    Identify metrics deviating >1 standard deviation (steps, sedentary, sleep, RHR).
                    Correlate with pain↑ or flareUpToday=true to find likely biomechanical or lifestyle triggers.

                    Each object in "flareUpTriggers" must always include:
                    {
                      "metric": "string",
                      "value": "string or number",
                      "deviation": "string",
                      "impact": "string"
                    }
                    If no triggers exist, return an empty array [].

                    **For "riskForecast":**
                    Always return an object with:
                    {
                      "risk": float between 0.0–1.0,
                      "bucket": "LOW" | "MODERATE" | "HIGH"
                    }
                    If not enough data, default to { "risk": 0.0, "bucket": "LOW" }.

                    **For "interventionsToday":**
                    Recommend 2–3 short interventions based on worsened categories (stretch, stress, posture).

                    ---

                    IMPORTANT JSON CONTRACT RULES
                    - Output must be **valid JSON**. No markdown, no explanations, no code fences.
                    - Use only these keys exactly as shown below.
                    - Do not rename, remove, or add new keys.
                    - Use empty arrays [] or default objects {} if data missing.
                    - Never output null values.
                    - Structure must strictly match the schema below.

                    ---

                    Output JSON format (return ONLY JSON, no text/markdown):

                    {
                      "improved": [],
                      "worsened": [],
                      "possibleCauses": [],
                      "actionableAdvice": [],
                      "todaysInsight": "",
                      "recoveryInsights": "",
                      "discProtectionScore": 0,
                      "discScoreExplanation": "",
                      "flareUpTriggers": [
                        {
                          "metric": "",
                          "value": "",
                          "deviation": "",
                          "impact": ""
                        }
                      ],
                      "riskForecast": {
                        "risk": 0.0,
                        "bucket": "LOW"
                      },
                      "interventionsToday": []
                    }

                    ---

                    Data to Analyze:

                    Current Day Input JSON (todayJson):
                    %s

                    7-Day Aggregated Context JSON (contextJson):
                    %s
                    """, FIELD_CONTEXT_EXTENDED, todayJson, contextJson);

            //Build request body:
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4o-mini");
            body.put("temperature", 0.7);

            //Create list of map:
            HashMap<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are an AI spine health assistant");

            HashMap<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

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

            String responseBody = response.getBody();
            System.out.println("Response Body:" + responseBody);
            log.info("AI response: {}", responseBody);
            var root = objectMapper.readTree(responseBody);
            String modelUsed = root.path("model").asText("unknown");
            int totalTokensUsed = root.path("usage").path("total_tokens").asInt(0);
            int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
            int completionToken = root.path("usage").path("completion_tokens").asInt(0);

            String content = root.path("choices").get(0).path("message").path("content").asText();
            content = content
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            AiInsightResponseDto insight = objectMapper.readValue(content, AiInsightResponseDto.class);
            List<FlareUpTriggersDto> flareUpTriggersDtos = insight.getFlareUpTriggers();
            List<String> improved = insight.getImproved();
            List<String> worsened = insight.getWorsened();
            List<String> possibleCauses = insight.getPossibleCauses();
            List<String> actionableAdvice = insight.getActionableAdvice();
            List<String> interventionsToday = insight.getInterventionsToday();
            RiskForecastDto riskForecastDto = insight.getRiskForecast();

            //Save the Ai insight in db:
            AiDailyInsight savedInsight = AiDailyInsight.builder()
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

            //Save the flare up triggers:
            if (flareUpTriggersDtos != null && !flareUpTriggersDtos.isEmpty()) {
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

                flareUpTriggersRepository.saveAll(flareUpEntries);
            }

            //Save improved:
            if (improved != null && !improved.isEmpty()) {
                List<AiDailyInsightImproved> improvedList = new ArrayList<>();
                for (int i = 0; i < improved.size(); i++) {
                    improvedList.add(
                            AiDailyInsightImproved.builder()
                                    .aiDailyInsight(savedInsight)
                                    .improved(improved.get(i))
                                    .build()
                    );
                }

                improvedRepository.saveAll(improvedList);
            }

            //Save worsened:
            if (worsened != null && !worsened.isEmpty()) {
                List<AiDailyInsightWorsened> worsenedList = new ArrayList<>();
                for (int i = 0; i < worsened.size(); i++) {
                    worsenedList.add(
                            AiDailyInsightWorsened.builder()
                                    .aiDailyInsight(savedInsight)
                                    .worsened(worsened.get(i))
                                    .build()
                    );
                }

                worsenedRepository.saveAll(worsenedList);
            }

            //Save possible causes:
            if (possibleCauses != null && !possibleCauses.isEmpty()) {
                List<AiDailyInsightPossibleCauses> possibleCausesList = new ArrayList<>();
                for (int i = 0; i < possibleCauses.size(); i++) {
                    possibleCausesList.add(
                            AiDailyInsightPossibleCauses.builder()
                                    .aiDailyInsight(savedInsight)
                                    .possibleCauses(possibleCauses.get(i))
                                    .build()
                    );
                }

                possibleCausesRepository.saveAll(possibleCausesList);
            }

            //Save actionable advices:
            if (actionableAdvice != null && !actionableAdvice.isEmpty()) {
                List<AiDailyInsightActionableAdvice> actionableAdvicesList = new ArrayList<>();
                for (int i = 0; i < actionableAdvice.size(); i++) {
                    actionableAdvicesList.add(
                            AiDailyInsightActionableAdvice.builder()
                                    .aiDailyInsight(savedInsight)
                                    .advice(actionableAdvice.get(i))
                                    .build()
                    );
                }

                actionableAdviceRepository.saveAll(actionableAdvicesList);
            }

            //Save interventions today:
            if (interventionsToday != null && !interventionsToday.isEmpty()) {
                List<AiDailyInsightInterventionsToday> interventionsTodaysList = new ArrayList<>();
                for (int i = 0; i < interventionsToday.size(); i++) {
                    interventionsTodaysList.add(
                            AiDailyInsightInterventionsToday.builder()
                                    .aiDailyInsight(savedInsight)
                                    .interventions(interventionsToday.get(i))
                                    .build()
                    );
                }

                interventionsTodayRepository.saveAll(interventionsTodaysList);
            }

            //Save risk forecast (one to one):
            if (riskForecastDto != null) {
                AiDailyInsightRiskForecasts riskForecasts = AiDailyInsightRiskForecasts.builder()
                        .aiDailyInsight(savedInsight)
                        .risk(riskForecastDto.getRisk())
                        .bucket(riskForecastDto.getBucket())
                        .build();

                riskForecastsRepository.save(riskForecasts);
            }

            log.info("AI Insights generated successfully for user: {}", dto.getId());
            return insight;
        } catch (Exception e) {
            log.error("AI generation failed: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to generate AI insights. Please try again later.", e);
        }
    }
}
