package com.fitspine.service.impl;

import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.exception.AiInsightApiLimitException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.User;
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
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            //Lazy loads succeed because we are in a transaction/session
            user.getUserInjuryList().size();
            user.getUserSurgeryList().size();
            user.getUserDiscIssueList().size();

            AiUserDailyInputDto dto = dailyAggregationService.buildAiInput(user.getEmail(), date);

            insightService.generateDailyInsight(dto, user.getEmail(), date);

            log.info("CRONJOB -> Worker -> AI Insight generated for user {}", user.getPublicId());
        } catch (AiInsightApiLimitException exception) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            log.info("CRONJOB -> WORKER -> user: {} has reached rate limit for the day to generate AI insight", user.getPublicId());
        } catch (Exception ex) {
            log.error("CRONJOB -> Worker FAILED for user {}: {}", userId, ex.getMessage());
        }
    }
}
