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
                    You are FitSpine AI, an advanced spine health and recovery intelligence system.

                    Goal:
                    Analyze biomechanical, neurological, and lifestyle data to identify patterns,
                    compare the user's current day (todayJson) with 7-day context (contextJson),
                    and generate structured, clinically meaningful recovery insights that a patient can read.

                    Clinical Reasoning Context (FitSpine AI knowledge base):
                    - FitSpine AI must use physiological and biomechanical reasoning when explaining any change, cause, or advice.
                    - Link all metric changes to plausible mechanisms like:
                      • inflammation or recovery (pain, stiffness, sleep, restingHeartRate)
                      • spinal load tolerance and disc pressure (sittingTime, liftingOrStrain)
                      • nervous system regulation (stressLevel, morningStiffness)
                      • tissue circulation and mobility (steps, activeMinutes)
                    - Use phrases like “which may indicate”, “suggesting”, or “likely due to” to show clinical reasoning.
                    - Every “possibleCause”, “actionableAdvice”, and “interventionToday” must explain the physiological *why*, not just what changed.
                    - NEVER copy or reuse example text or phrasing shown anywhere in this prompt. Generate original, context-specific sentences every time.
                    - Do NOT sound generic. Always reference the metric values and the underlying spine-related mechanism.
                    - Avoid speculative language like “might be” or “maybe”; use assertive but cautious phrasing like “likely due to” or “consistent with”.
                    - Ensure advice is spine-safe and recovery-oriented:
                      - Encourage gentle mobility, short walks, and neutral-spine movements.
                      - Avoid recommending heavy lifting, deep flexion, or twisting.
                    - For flareUpTriggers, connect deviations to biomechanical stress or recovery deficits (e.g., “lower sleep and higher heart rate may signal incomplete recovery”).

                    ---

                    FIELD CONTEXT (definitions, scaling, and interpretation):
                    %s

                    ---

                    Strict Interpretation Rules:
                    - A value of -1 in either todayJson or contextJson means "no data". Skip that metric for comparisons and scoring.
                    - Compare only metrics present in both JSONs and not equal to -1.
                    - Ordinal scales (lower is better): painLevel (0–3), morningStiffness (0–3), stressLevel (0–4), sittingTime (0–4).
                    - Quantitative directions:
                      lower is better: sedentaryMinutes, restingHeartRate
                      higher is better: steps, activeMinutes, totalMinutesAsleep, efficiency
                    - Use the window metadata from contextJson when writing: windowDays, daysAvailable, startDate, endDate.
                      Say “compared with your last {daysAvailable} days in a {windowDays}-day window (from {startDate} to {endDate})”.
                    - When writing explanations, use plain language and units. Do NOT use symbols like %%, ±, →, ↑, ↓, or parentheses math.
                      Prefer phrases like “about 1.9 thousand more steps” or “around six points lower” instead of “(+1,854, +32%%)”.

                    Delta Computation:
                    - For each comparable metric M:
                      * If lower-is-better (painLevel, stressLevel, morningStiffness, sittingTime, sedentaryMinutes, restingHeartRate):
                          delta = contextAvg(M) - today(M). Positive delta = improved.
                      * If higher-is-better (steps, activeMinutes, totalMinutesAsleep, efficiency):
                          delta = today(M) - contextAvg(M). Positive delta = improved.
                    - Also compute a rough verbal magnitude (“slightly”, “clearly”, “markedly”) based on absolute delta vs context average.
                    - Never misstate direction: if today < average, call it “lower”; if today > average, call it “higher”.

                    Significance Thresholds (reduce noise):
                    - Ordinals: change of at least 1 category is significant.
                    - Quantitative minimum absolute deltas (use max(floor, 5%% of context average)):
                      steps: max(800, 5%%)
                      activeMinutes: max(10, 5%%)
                      sedentaryMinutes: max(30, 5%%)
                      totalMinutesAsleep: max(30, 5%%)
                      efficiency: max(3 points, 5%%)
                      restingHeartRate: max(2 bpm, 5%%)
                    - If no metric crosses thresholds but comparable data exists, still select the single best improvement and the single worst worsening by absolute delta so outputs are not empty. In narratives, state that changes are small.

                    Personalization Constraints (use if provided in todayJson):
                    - If injuryTypes or discLevels include disc pathology (e.g., bulging/herniated/extrusion/annular tear), avoid recommendations involving sustained spinal flexion, loaded rotation, or high axial compression. Prefer neutral-spine strategies, spine-sparing patterns, and options like McGill Big Three, gentle walking, short mobility breaks.
                    - If hasSurgeryHistory=true or surgeryTypes present, keep advice conservative and emphasize gradual progression, symptom monitoring, and surgeon/PT clearance where relevant.
                    - Always tailor actionableAdvice and interventionsToday to these constraints.

                    ---

                    SECTION RULES (STRICT) — keep the response schema exactly as given later:

                    "improved" (array of strings):
                    - For each improved metric, provide a short human sentence per item, not just the name.
                    - Format: "steps — about {todayMinusAvg} more than your typical day over the last {daysAvailable} days (today {todayValue}, typical {avgValue})."
                    - Use words like “about”, “around”, “slightly”, “clearly” instead of symbols or percentages.
                    - Add a brief physiological explanation for each improvement (e.g., “suggesting better circulation or recovery”).

                    "worsened" (array of strings):
                    - For each worsened metric, provide a short human sentence per item, not just the name.
                    - Format: "painLevel — worse by one level (today {labelToday} vs {labelAvg}) compared with your last {daysAvailable} days."
                    - For quantitative: "sedentaryMinutes — about {absDelta} more minutes than typical (today {todayValue}, typical {avgValue})."
                    - Add a brief clinical reason (e.g., “which may indicate increased load or incomplete recovery”).

                    "todaysInsight":
                    - 2–4 concise sentences summarizing the 2–4 biggest changes using plain words and numbers with units.
                    - Example style: "You walked about 1.9 thousand more steps than your typical day over the last 6 days. Sleep time was roughly the same, but sleep efficiency was about six points lower. Pain and morning stiffness each worsened by one level."

                    "recoveryInsights":
                    - 2–4 sentences interpreting the pattern and its clinical meaning (e.g., better activity but higher pain/stiffness suggests load tolerance is limited; lower efficiency plus higher stress suggests poorer overnight recovery).
                    - Mention the window explicitly: “compared with your last {daysAvailable} days in a {windowDays}-day window.”

                    "possibleCauses":
                    - Provide 2–4 sentences. Each item must be a full sentence using “because” and the actual compared values.
                    - Explain *why* in clinical terms: reference inflammation, disc pressure, fatigue, or recovery deficit.
                    - Example style (adapt numbers and levels):
                      - "Pain likely increased because sitting time category moved higher today compared with your typical last {daysAvailable} days, increasing disc pressure and muscle tension."
                      - "Higher stress today alongside slightly lower sleep efficiency may have heightened pain sensitivity."
                    - NEVER copy example text or templates — generate fresh, context-specific explanations every time.

                    "actionableAdvice":
                    - Provide exactly 3 concrete, clinically sound, metric-driven items tied to TODAY’S worsened set or the clearest negative deltas.
                    - Each item must include WHAT to do, HOW MUCH/HOW LONG/WHEN, and WHY it fits the user’s data and conditions (injuries/surgery if present).
                    - Include the physiological rationale (e.g., “to reduce disc pressure”, “to enhance recovery”, “to calm nervous system reactivity”).
                    - Do not repeat a generic list; tailor to today's numbers and the user’s conditions.

                    "flareUpTriggers":
                    - Identify anomalies where today deviates from the context average by more than 1 standard deviation for metrics with SD provided (steps, sedentary, sleep, restingHeartRate).
                    - Each trigger object must include:
                      {
                        "metric": "<name>",
                        "value": "today {todayValue} vs typical {avgValue} over the last {daysAvailable} days; about {absDelta} difference; {zScoreText if SD available}",
                        "deviation": "above or below typical by {zScoreRounded} SD (or 'N/A' if SD unavailable)",
                        "impact": "connect the deviation to a plausible biomechanical or physiological explanation, such as poor sleep reducing tissue recovery or increased sitting elevating disc pressure."
                      }
                    - If no SD is available for any candidate but deltas exist, pick the largest absolute delta and set deviation='N/A' with a qualitative impact.
                    - Avoid contradictions: if today < average, do not say “higher”.

                    "discProtectionScore":
                    - Start at 70, then:
                      +5 each: pain/stress/stiffness improved, stretchingDone=true, steps higher, totalMinutesAsleep higher, efficiency higher, restingHeartRate lower.
                      −5 each: pain/stress/stiffness worsened, sittingTime higher or sedentaryMinutes higher, totalMinutesAsleep lower or efficiency lower, restingHeartRate higher, flareUpToday=true.
                    - Clamp 0–100.
                    - In "discScoreExplanation", explicitly name 2–4 main contributors and their directions in plain language (no symbols).

                    "interventionsToday":
                    - MUST contain 2–3 brief, specific actions for today. Never return an empty array.
                    - Each item should be 1 sentence, actionable, and compatible with the user’s injuries/surgery status.
                    - Include reasoning (e.g., “to decompress the spine”, “to improve circulation”, “to calm the nervous system”).
                    - NEVER copy or reuse example text or phrasing.

                    "riskForecast":
                    - Keep the same structure as provided. If insufficient data, keep LOW.

                    Hard Requirements:
                    - Never output nulls.
                    - If comparable metrics exist, "improved" and "worsened" must not both be empty; include the closest small changes if needed and note they are small in narrative.
                    - Use plain language; DO NOT use percent signs, mathematic parentheses, or arrow glyphs.
                    - Keep the exact output JSON schema below; do not rename, remove, or add keys.

                    ---

                    Output JSON format (return ONLY JSON, no markdown, no code fences):

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

                    Current Day Input JSON (todayJson; numeric fields use -1 if no data):
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

            log.info("AI Insights generated successfully for user: {}", dto.getId());
            return insight;
        } catch (Exception e) {
            log.error("AI generation failed: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to generate AI insights. Please try again later.", e);
        }
    }
}
