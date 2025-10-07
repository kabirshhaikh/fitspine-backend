package com.fitspine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.dto.AiInsightResponseDto;
import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.service.AiInsightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public AiInsightServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiInsightResponseDto generateDailyInsight(AiUserDailyInputDto dto) {
        try {
            String userJson = objectMapper.writeValueAsString(dto);

            //Build AI prompt:
            String prompt = """
                    You are FitSpine AI — a specialized spine health assistant that analyzes a user's daily spine log.

                    Your job is to interpret biomechanical, neurological, and behavioral data to generate meaningful insights for spinal health and recovery.

                    Your objectives:
                    1. Read the user's profile: gender, age, injuries, surgeries, and disc levels.
                    2. Analyze activity, heart, and sleep metrics together to understand daily spine recovery.
                    3. Identify *causal flare-up triggers* with reasoning (e.g., stress, posture, over-sitting, low mobility, poor recovery).
                    4. Write a **Today's Insight** section — a short summary (1–2 sentences) describing what went well or poorly overall.
                    5. Write detailed **Recovery Insights** — empathetic, science-backed guidance explaining the user’s data patterns, recovery state, and prevention tips.
                    6. Assign a **Disc Protection Score (0–100)** reflecting spinal resilience and risk for that day.
                    7. Add a short **Disc Score Explanation** describing what most influenced the score (stress, sleep, activity, etc.).
                    8. Always respond with factual, encouraging tone and biomechanical reasoning.
                    9. Return **strict JSON only** — no markdown, no code blocks.

                    Input JSON:
                    %s

                    Output strictly as JSON:
                    {
                      "todaysInsight": "Short 1–2 sentence highlight of what went right or wrong today.",
                      "flareUpTriggers": ["list of detailed causal factors"],
                      "recoveryInsights": "Detailed explanation linking today's data patterns to advice and recovery factors.",
                      "discProtectionScore": 0,
                      "discScoreExplanation": "Short reasoning describing how the score was calculated based on posture, stress, sleep, and activity."
                    }
                    """.formatted(userJson);


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
            log.info("AI response: {}", responseBody);
            var root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            content = content
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            AiInsightResponseDto insight = objectMapper.readValue(content, AiInsightResponseDto.class);
            log.info("AI Insights generated successfully for user: {}", dto.getId());
            return insight;
        } catch (Exception e) {
            log.error("AI generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("AI generation failed", e);
        }
    }
}
