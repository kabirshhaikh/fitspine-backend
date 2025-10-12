package com.fitspine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.dto.AiInsightResponseDto;
import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.dto.FitbitAiContextInsightDto;
import com.fitspine.exception.AiServiceException;
import com.fitspine.exception.ResourceNotFoundException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.AiInsightHelper;
import com.fitspine.model.AiDailyInsight;
import com.fitspine.model.AiDailyInsightFlareUpTriggers;
import com.fitspine.model.User;
import com.fitspine.repository.AiDailyInsightFlareUpTriggersRepository;
import com.fitspine.repository.AiDailyInsightRepository;
import com.fitspine.repository.UserRepository;
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

    public AiInsightServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, AiDailyInsightRepository insightRepository, UserRepository userRepository, AiInsightHelper aiHelper, AiDailyInsightFlareUpTriggersRepository flareUpTriggersRepository, FitbitContextAggregationService fitbitContextAggregationService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.insightRepository = insightRepository;
        this.flareUpTriggersRepository = flareUpTriggersRepository;
        this.userRepository = userRepository;
        this.aiHelper = aiHelper;
        this.fitbitContextAggregationService = fitbitContextAggregationService;
    }

    private static final String FIELD_CONTEXT = """
            Context (field meanings):
            - restingHeartRate: average resting heart rate (in beats per minute, indicates cardiovascular recovery and stress level)
            - caloriesOut: total calories burned (BMR + activity)
            - activityCalories: calories from intentional exercise
            - caloriesBMR: baseline metabolism at rest
            - marginalCalories: calories from light, non-exercise movement
            - sedentaryMinutes: total inactive minutes in the day
            - steps: total steps walked in the day
            - lightlyActiveMinutes, fairlyActiveMinutes, veryActiveMinutes: activity intensity levels in minutes
            - totalMinutesAsleep: total minutes spent asleep
            - totalTimeInBed: total minutes in bed (sleep + awake)
            - efficiency: sleep quality percentage (0–100)
            - startTime, endTime: sleep window timestamps
            - minutesAsleep, minutesAwake, minutesToFallAsleep, timeInBed: detailed durations (all in minutes)
            - isMainSleep: true if primary overnight sleep, false if nap

            Scoring Reference (Enum-based numeric scores):
            - painLevel: 0=None, 1=Mild, 2=Moderate, 3=Severe
            - sittingTime: 0=<2h, 1=2–4h, 2=4–6h, 3=6–8h, 4=>8h
            - standingTime: 0=<2h, 1=2–4h, 2=4–6h, 3=6–8h, 4=>8h
            - morningStiffness: 0=None, 1=Mild, 2=Moderate, 3=Severe
            - stressLevel: 0=Very Low, 1=Low, 2=Moderate, 3=High, 4=Very High

            Notes:
            - All averages in contextJson (e.g., averagePainLevel, averageStressLevel) are derived from these scaled numeric values.
            - Higher values indicate worse conditions for pain, stress, stiffness, sitting time, and standing time.
            - Lower values indicate better or healthier outcomes.
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
            List<String> triggers = aiHelper.returnTriggerText(flareUpTriggers);
            return AiInsightResponseDto.builder()
                    .todaysInsight(insight.getTodaysInsights() != null ? insight.getTodaysInsights() : "")
                    .flareUpTriggers(triggers != null ? triggers : new ArrayList<>())
                    .recoveryInsights(insight.getRecoveryInsights() != null ? insight.getRecoveryInsights() : "")
                    .discProtectionScore(insight.getDiscProtectionScore() != null ? insight.getDiscProtectionScore() : 0)
                    .discScoreExplanation(insight.getDiscScoreExplanation() != null ? insight.getDiscScoreExplanation() : "")
                    .build();
        }

        FitbitAiContextInsightDto contextDto = fitbitContextAggregationService.buildContext(email, logDate); //Here logDate is he currentDate meaning target date

        try {
            String todayJson = objectMapper.writeValueAsString(dto);
            String contextJson = objectMapper.writeValueAsString(contextDto);

            log.info("Today's json: {}", todayJson);
            log.info("Context json: {}", contextJson);

            //Build AI prompt:
            String prompt = String.format("""
                    You are **FitSpine AI**, an advanced spine health and recovery intelligence system.

                    Your goal: analyze biomechanical, neurological, and lifestyle data to identify patterns,
                    compare the user's *current day* with *7-day context averages*, and generate structured,
                    clinically useful recovery insights.

                    ---

                    Data You Receive
                    - **todayJson** → user's full daily log (symptoms + Fitbit data)
                    - **contextJson** → 7-day aggregated averages and percentages

                    ---

                    Step 1: Field-by-Field Comparison
                    For every numeric or categorical metric that appears in both JSONs,
                    perform a directional comparison:

                    - If today's value > context average → label as "worsened"  
                    - If today's value < context average → label as "improved"  
                    - If approximately equal (±10%%) → label as "stable"

                    Metrics to compare include (but are not limited to):
                    painLevel, stressLevel, morningStiffness, sittingTime, standingTime,
                    restingHeartRate, caloriesOut, steps, sedentaryMinutes, activeMinutes,
                    totalMinutesAsleep, efficiency.

                    ---

                    Step 2: Detect Cause–Effect Patterns
                    Using reasoning, look for meaningful relationships such as:

                    - pain ↑ + stress ↑ → stress-related flare-up  
                    - pain ↑ + sittingTime ↑ → posture strain  
                    - stiffness ↑ + low sleep → poor overnight recovery  
                    - numbnessTingling + liftingOrStrain → mechanical nerve irritation  
                    - stress ↓ + steps ↑ → positive adaptation  
                    - RHR ↓ + good sleep → systemic recovery  

                    You may infer other clinically plausible spine health relationships.

                    ---

                    Step 3: Evaluate Recovery Direction
                    From the comparison and patterns, infer what is:
                    - **Improving** (positive trend vs baseline)
                    - **Worsening** (negative trend)
                    - **Stable** (no major deviation)

                    ---

                    Step 4: Generate Insights
                    Produce insights with a human-readable explanation for each section:
                    1. **What Improved:** list top 2–3 areas showing positive change.  
                    2. **What Worsened:** list top 2–3 areas of concern.  
                    3. **Possible Causes:** explain *why* (based on detected patterns).  
                    4. **Actionable Advice:** short behavioral recommendations
                       (stretching, rest, activity balance, stress control, sleep improvement).  

                    ---

                    Step 5: Compute Disc Protection Score
                    Assign a score 0–100 for today's spinal resilience using this reasoning guideline:
                    - + points for low pain/stress/stiffness, adequate sleep, balanced activity, low RHR
                    - − points for high pain/stress, poor sleep, long sitting, high RHR, flare-up
                    Also provide a one-sentence **Disc Score Explanation** summarizing which factors most influenced the score.

                    ---

                    Step 6: Output Format
                    Respond **only** as pure JSON (no markdown, no text, no code fences):

                    {
                      "improved": ["list of metrics or habits improving"],
                      "worsened": ["list of metrics or habits worsening"],
                      "possibleCauses": ["list of likely causes or correlations"],
                      "actionableAdvice": ["short bullet recommendations"],
                      "todaysInsight": "1–2 sentence summary of today's spine condition",
                      "recoveryInsights": "2–3 sentence deeper analysis connecting today's data to weekly trends",
                      "discProtectionScore": 0,
                      "discScoreExplanation": "short reasoning about the score"
                    }

                    ---

                    Step 7: Data to Analyze
                    FIELD CONTEXT (definitions and scaling):
                    %s

                    =====
                    Current Day Input JSON (todayJson):
                    %s

                    =====
                    7-Day Aggregated Context JSON (contextJson):
                    %s
                    """, FIELD_CONTEXT, todayJson, contextJson);


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
            List<String> triggers = insight.getFlareUpTriggers();

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
            if (triggers != null && !triggers.isEmpty()) {
                List<AiDailyInsightFlareUpTriggers> flareUpEntries = new ArrayList<>();
                for (int i = 0; i < triggers.size(); i++) {
                    flareUpEntries.add(
                            AiDailyInsightFlareUpTriggers.builder()
                                    .aiDailyInsight(savedInsight)
                                    .triggerText(triggers.get(i))
                                    .build()
                    );
                }

                flareUpTriggersRepository.saveAll(flareUpEntries);
            }

            log.info("AI Insights generated successfully for user: {}", dto.getId());
            return insight;
        } catch (Exception e) {
            log.error("AI generation failed: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to generate AI insights. Please try again later.", e);
        }
    }
}
