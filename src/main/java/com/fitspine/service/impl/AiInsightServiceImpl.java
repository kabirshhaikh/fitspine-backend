package com.fitspine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.dto.*;
import com.fitspine.exception.AiInsightApiLimitException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiInsightServiceImpl implements AiInsightService {
    @Value("${openai.api.key}")
    private String apiKey;
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final ObjectMapper objectMapper;
    private final AiDailyInsightRepository insightRepository;
    private final UserRepository userRepository;
    private final AiInsightHelper aiHelper;
    private final FitbitContextAggregationService fitbitContextAggregationService;

    private final RedisTemplate<String, Object> redis;


    public AiInsightServiceImpl(
            ObjectMapper objectMapper,
            AiDailyInsightRepository insightRepository,
            UserRepository userRepository,
            AiInsightHelper aiHelper,
            FitbitContextAggregationService fitbitContextAggregationService,
            RedisTemplate<String, Object> redis
    ) {
        this.objectMapper = objectMapper;
        this.insightRepository = insightRepository;
        this.userRepository = userRepository;
        this.aiHelper = aiHelper;
        this.fitbitContextAggregationService = fitbitContextAggregationService;
        this.redis = redis;
    }

    public static final int DAILY_LIMIT = 3;

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
                
            ---------------------------------------    
            CRITICAL FORMAT RULE (MANDATORY):
            The fields improved, worsened, possibleCauses, actionableAdvice, and interventionsToday MUST be arrays of plain strings ONLY.\s
            If you attempt to output objects instead of strings, STOP and regenerate the entire JSON using the correct format.\s
            Never output objects inside these arrays.
                        
            Analyze them using ALL rules above and return ONLY the final JSON output in the exact schema shown above.
            """, FIELD_CONTEXT_EXTENDED);


    @Transactional
    @Override
    public AiInsightResponseDto generateDailyInsight(AiUserDailyInputDto dto, String email, LocalDate logDate) {
        //Get the user:
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        //Redis keys:
        String insightKey = "ai_insight:" + user.getPublicId() + ":" + logDate;
        String hashKey = "ai_insight_hash:" + user.getPublicId() + ":" + logDate;
        String limitKey = "ai_insight_api_call:" + user.getPublicId() + ":" + logDate;

        //AI call limit check:
        Long count = redis.opsForValue().increment(limitKey);

        if (count != null && count == 1) {
            //Current date time in UTC:
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

            //Calculate the start of next day in UTC: 12 am basically:
            ZonedDateTime nextMidNightUtc = now.toLocalDate().atStartOfDay(ZoneOffset.UTC).plusDays(1);

            //Calculate the different between now and then:
            Duration timeUntilMidnight = Duration.between(now, nextMidNightUtc);

            //Set redis expiry:
            redis.expire(limitKey, timeUntilMidnight);
        }

        if (count != null && count > DAILY_LIMIT) {
            throw new AiInsightApiLimitException("You have reached your AI Insight limit for the day.");
        }

        //Build context:
        FitbitAiContextInsightDto contextDto = fitbitContextAggregationService.buildContext(email, logDate); //Here logDate is he currentDate meaning target date

        try {
            //Map dto's to json:
            String todayJson = objectMapper.writeValueAsString(dto);
            String contextJson = objectMapper.writeValueAsString(contextDto);
            log.info("Today's json: {}", todayJson);
            log.info("Context json: {}", contextJson);

            //Create sha256:
            String raw = todayJson + "_" + contextJson;
            String newHash = aiHelper.sha256(raw);

            //Fetch existing hash key from redis:
            String existingHash = (String) redis.opsForValue().get(hashKey);
            log.info("Existing HASH {}", existingHash);

            //If existing has equals new hash then return cache insights:
            if (existingHash != null && existingHash.equals(newHash)) {

                AiInsightResponseDto cachedResponse = (AiInsightResponseDto) redis.opsForValue().get(insightKey);

                if (cachedResponse != null) {
                    log.info("HASH MATCH -> Returning cache insights");
                    return cachedResponse;
                }

                //If redis is missing cache and hash is same then load response dto with db:
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

                    AiInsightResponseDto cacheFailResponse = AiInsightResponseDto.builder()
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

                    //Set the redis with db data since redis was missing the cache:
                    redis.opsForValue().set(insightKey, cacheFailResponse, Duration.ofHours(12));

                    //Return the dto mapped from db:
                    return cacheFailResponse;
                }
            }

            //If HASH is different then generate new insights:
            log.info("HASH IS DIFFERENT, generating new AI insights for the user {} on date {}", user.getPublicId(), logDate);

            //Make call to open ai and get the response:
            String responseBody = aiHelper.callOpenAi(todayJson, contextJson, SYSTEM_PROMPT, apiKey, OPENAI_URL);
            log.info("AI response: {}", responseBody);

            //Save the data using helper function:
            AiInsightResponseDto insight = aiHelper.saveDataForAiInsight(user, logDate, responseBody);

            //Set redis keys:
            redis.opsForValue().set(insightKey, insight, Duration.ofHours(12));
            redis.opsForValue().set(hashKey, newHash, Duration.ofHours(12));

            //Return dto response from saved data:
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