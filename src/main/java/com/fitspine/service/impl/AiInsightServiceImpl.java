package com.fitspine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.dto.*;
import com.fitspine.exception.AiInsightApiLimitException;
import com.fitspine.exception.AiServiceException;
import com.fitspine.exception.ResourceNotFoundException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.AiInsightHelper;
import com.fitspine.model.*;
import com.fitspine.prompt.AiPrompt;
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
                                .flareUpRiskScore(insight.getRiskForecasts().getFlareUpRiskScore())
                                .painRiskScore(insight.getRiskForecasts().getPainRiskScore())
                                .riskBucket(insight.getRiskForecasts().getRiskBucket())
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
            String responseBody = aiHelper.callOpenAi(todayJson, contextJson, AiPrompt.FITBIT_SYSTEM_PROMPT, apiKey, OPENAI_URL);
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