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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
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
    private final StringRedisTemplate redisTemplate;
    private final RedisTemplate<String, Object> redis;


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
            AiDailyInsightRiskForecastsRepository riskForecastsRepository,
            StringRedisTemplate redisTemplate,
            RedisTemplate<String, Object> redis
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
        this.redisTemplate = redisTemplate;
        this.redis = redis;
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
            - notes: User’s textual de-identified remarks about the day

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

    public static final String SYSTEM_PROMPT = String.format("""
            You are FitSpine AI, an advanced spine health and recovery intelligence system.

            Goal:
            Analyze biomechanical, neurological, and lifestyle data, compare the user’s current day with their 7-day context, and produce structured, clinically meaningful recovery insights that follow all rules below.

            ---------------------------------------
            CLINICAL REASONING REQUIREMENTS
            ---------------------------------------
            - Always use biomechanical and physiological reasoning.
            - Link metric changes to mechanisms such as inflammation, disc pressure, load tolerance, sleep recovery, nervous-system regulation, or circulation.
            - Use phrases like “which may indicate”, “suggesting”, “consistent with”, “likely due to”.
            - Writing must be original and context-specific; never reuse example text.
            - Advice must always be spine-safe:
              • Encourage gentle movement, walking, neutral-spine mobility.
              • Avoid recommending heavy lifting, twisting, or deep spinal flexion.

            ---------------------------------------
            FIELD CONTEXT (definitions, scaling)
            ---------------------------------------
            %s

            ---------------------------------------
            INTERPRETATION RULES
            ---------------------------------------
            - A value of -1 in either todayJson or contextJson = “no data”. Skip that metric.
            - Compare only metrics present in both JSONs and not equal to -1.
            - Ordinal metrics (lower = better): painLevel, morningStiffness, stressLevel, sittingTime.
            - Quantitative metrics:
              • lower = better: sedentaryMinutes, restingHeartRate
              • higher = better: steps, activeMinutes, totalMinutesAsleep, efficiency
            - Use window metadata (windowDays, daysAvailable, startDate, endDate) when describing comparisons.
            - Use plain language only; never use percent symbols or math notation.

            ---------------------------------------
            DELTA LOGIC
            ---------------------------------------
            - Lower-is-better metrics → delta = contextAverage − todayValue.
            - Higher-is-better metrics → delta = todayValue − contextAverage.
            - Positive delta = improved.
            - Magnitude words (“slightly”, “clearly”, “markedly”) must match size of change.
            - Never reverse direction; if today < average say “lower”, not “higher”.

            ---------------------------------------
            SIGNIFICANCE THRESHOLDS
            ---------------------------------------
            - Ordinals: change of ≥1 category = significant.
            - Quantitative minimum absolute deltas (max(floor value, 5%% of context average)):
              steps: 800
              activeMinutes: 10
              sedentaryMinutes: 30
              totalMinutesAsleep: 30
              efficiency: 3
              restingHeartRate: 2
            - If no metric crosses thresholds:
              • still identify the biggest improvement
              • and the biggest worsening
              and state that these changes are small.

            ---------------------------------------
            PERSONALIZATION CONSTRAINTS
            ---------------------------------------
            - If disc issues exist: avoid any recommendation involving flexion, twisting, or compression.
            - If surgery history exists: advice must be conservative, gradual, and safety-oriented.
            - All actionableAdvice and interventionsToday must obey these constraints.

            ---------------------------------------
            SECTION RULES (STRICT)
            ---------------------------------------

            "improved":
            - Must be an ARRAY OF STRINGS ONLY.
            - Each item must state:
              • the metric,
              • today’s value vs context average (plain language),
              • the physiological improvement mechanism (e.g., reduced inflammation, lower disc load, improved circulation, better autonomic recovery).
            - Must use doctor-level biomechanical reasoning.

            "worsened":
            - Must be an ARRAY OF STRINGS ONLY.
            - Each item must state:
              • the metric,
              • today vs average,
              • the clinical implication (e.g., increased nociceptive signaling, elevated sympathetic arousal, disturbed sleep recovery, heightened spinal load).
            - Must be medically realistic and spine-safe.

            "todaysInsight":
            - 2–4 sentences summarizing the most clinically meaningful patterns.
            - Must interpret inflammation, neural sensitivity, load tolerance, or sleep recovery patterns.
            - Use numbers in plain English (no symbols).

            "recoveryInsights":
            - 2–4 sentences explaining what today means for ongoing recovery, disc load tolerance, and nerve irritation trends.
            - Must reference the rolling window (daysAvailable, windowDays).
            - Must read like a physiotherapist’s clinical reasoning.

            "possibleCauses":
            - 2–4 full sentences.
            - Each must explain:
              • WHAT changed,
              • WHY it changed (today vs average),
              • WHAT underlying mechanism explains it (disc pressure, inflammation, neural sensitivity, mechanical loading, recovery deficit).
            - Must use “because” every time.
            - Never generic or reused text.

            "actionableAdvice":
            - Exactly 3 items.
            - Must reflect current-day issues (from worsened metrics or biggest negative deltas).
            - Each item must include:
              • WHAT to do,
              • HOW LONG / HOW MUCH / WHEN,
              • WHY (physiological rationale — e.g., reduce nerve irritation, decompress the spine, restore mobility without provoking symptoms).
            - MUST be spine-safe and tailored to disc pathology or surgery status.

            "flareUpTriggers":
            - Use SD if available; otherwise pick the largest delta.
            - Each object must contain:
              metric, value, deviation, impact.
            - “impact” must explain the biomechanical or physiological consequence (e.g., inadequate sleep slowing tissue repair, increased sitting elevating disc compression).
            - No contradictions or generic statements.

            "discProtectionScore":
            - Start at 70 and apply ±5 rules.
            - Score must reflect biomechanical safety and recovery conditions.
            - Clamp 0–100.

            "discScoreExplanation":
            - 2–4 clear, medical-grade statements explaining primary contributors to the score.

            "interventionsToday":
            - MUST be 2–3 short, medical-grade, spine-safe actions.
            - Must include a biomechanical or physiological rationale.
            - Never generic wellness tips.

            "riskForecast":
            - Use the provided structure.
            - If insufficient data, bucket = LOW.
            - Otherwise reason clinically (e.g., pain rising + poorer sleep + elevated RHR = elevated risk).

            ---------------------------------------
            ABSOLUTE JSON SHAPE REQUIREMENTS (STRONG)
            ---------------------------------------
            - Arrays MUST remain arrays even if there is only one item.
            - These keys MUST ALWAYS be arrays of plain strings:
              improved, worsened, possibleCauses, actionableAdvice, interventionsToday
            - Never output an object where an array is expected.
            - Never omit brackets.
            - Never rename keys.
            - Never add new keys.

            ---------------------------------------
            REQUIRED OUTPUT FORMAT (RETURN ONLY JSON)
            ---------------------------------------
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

            ---------------------------------------
            YOUR TASK
            ---------------------------------------
            You will receive two JSON objects:
            • todayJson
            • contextJson

            Analyze them using ALL rules above and return ONLY the final JSON output in the exact schema shown above.
            """, FIELD_CONTEXT_EXTENDED);


    @Transactional
    @Override
    public AiInsightResponseDto generateDailyInsight(AiUserDailyInputDto dto, String email, LocalDate logDate) {
        //Get the user:
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        //Redis key:
        String key = "ai_insights:" + user.getPublicId() + ":" + logDate;

        AiInsightResponseDto cacheResponse = (AiInsightResponseDto) redis.opsForValue().get(key);

        if (cacheResponse != null) {
            log.info("Cache HIT for {}", key);
            return cacheResponse;
        }

        log.info("Cache MISS for {}", key);

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

            if (cacheResponse == null) {
                log.info("Writing CACHE into redis: ");
                redis.opsForValue().set(key, insight, Duration.ofHours(12));
            }

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
                List<AiDailyInsightFlareUpTriggers> flareUpEntries = aiHelper.getFlareUpTriggers(flareUpTriggersDtos, savedInsight);
                flareUpTriggersRepository.saveAll(flareUpEntries);
            }

            //Save improved:
            if (improved != null && !improved.isEmpty()) {
                List<AiDailyInsightImproved> improvedList = aiHelper.getImprovedList(improved, savedInsight);
                improvedRepository.saveAll(improvedList);
            }

            //Save worsened:
            if (worsened != null && !worsened.isEmpty()) {
                List<AiDailyInsightWorsened> worsenedList = aiHelper.getWorsened(worsened, savedInsight);
                worsenedRepository.saveAll(worsenedList);
            }

            //Save possible causes:
            if (possibleCauses != null && !possibleCauses.isEmpty()) {
                List<AiDailyInsightPossibleCauses> possibleCausesList = aiHelper.getPossibleIssues(possibleCauses, savedInsight);
                possibleCausesRepository.saveAll(possibleCausesList);
            }

            //Save actionable advices:
            if (actionableAdvice != null && !actionableAdvice.isEmpty()) {
                List<AiDailyInsightActionableAdvice> actionableAdvicesList = aiHelper.getActionableAdvice(actionableAdvice, savedInsight);
                actionableAdviceRepository.saveAll(actionableAdvicesList);
            }

            //Save interventions today:
            if (interventionsToday != null && !interventionsToday.isEmpty()) {
                List<AiDailyInsightInterventionsToday> interventionsTodaysList = aiHelper.getInterventionsToday(interventionsToday, savedInsight);
                interventionsTodayRepository.saveAll(interventionsTodaysList);
            }

            //Save risk forecast (one to one):
            if (riskForecastDto != null) {
                AiDailyInsightRiskForecasts riskForecasts = AiDailyInsightRiskForecasts.builder().aiDailyInsight(savedInsight).risk(riskForecastDto.getRisk()).bucket(riskForecastDto.getBucket()).build();
                riskForecastsRepository.save(riskForecasts);
            }

//            log.info("AI Insights generated successfully for user: {}", dto.getId());
            return insight;
        } catch (Exception e) {
            log.error("AI generation failed: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to generate AI insights. Please try again later.", e);
        }
    }

    @Override
    public WeeklyGraphDto weeklyGraph(LocalDate date, String email) {
        //Get the user:
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return fitbitContextAggregationService.generateWeeklyGraph(date, user);
    }
}
