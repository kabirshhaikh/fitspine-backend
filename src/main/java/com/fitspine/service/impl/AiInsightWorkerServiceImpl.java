package com.fitspine.service.impl;

import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.exception.AiInsightApiLimitException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.repository.UserRepository;
import com.fitspine.service.AiInsightService;
import com.fitspine.service.AiInsightWorkerService;
import com.fitspine.service.FitbitAiDailyAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
public class AiInsightWorkerServiceImpl implements AiInsightWorkerService {
    private final FitbitAiDailyAggregationService dailyAggregationService;
    private final AiInsightService insightService;
    private final UserRepository userRepository;

    public AiInsightWorkerServiceImpl(
            FitbitAiDailyAggregationService dailyAggregationService,
            AiInsightService insightService,
            UserRepository userRepository
    ) {
        this.dailyAggregationService = dailyAggregationService;
        this.insightService = insightService;
        this.userRepository = userRepository;
    }

    @Transactional
    @Async("aiInsightCron")
    @Override
    public void processUserForCronjob(Long userId, LocalDate date) {
        try {
            String userEmail = userRepository.findEmailById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            String userPublicId = userRepository.findPublicIdById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            AiUserDailyInputDto dto = dailyAggregationService.buildAiInput(userEmail, date);

            insightService.generateDailyInsight(dto, userEmail, date);

            log.info("CRONJOB -> Worker -> AI Insight generated for user {}", userPublicId);
        } catch (AiInsightApiLimitException exception) {
            String userPublicId = userRepository.findPublicIdById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            log.info("CRONJOB -> WORKER -> user: {} has reached rate limit for the day to generate AI insight", userPublicId);

            return;
        } catch (Exception ex) {
            log.error("CRONJOB -> Worker FAILED for user {}: {}", userId, ex.getMessage());
        }
    }
}
