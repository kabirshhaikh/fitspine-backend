package com.fitspine.scheduler;

import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.model.User;
import com.fitspine.repository.AiDailyInsightRepository;
import com.fitspine.repository.UserRepository;
import com.fitspine.service.AiInsightService;
import com.fitspine.service.FitbitAiDailyAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class FitbitAiInsightScheduler {
    private final AiDailyInsightRepository insightRepository;
    private final AiInsightService aiInsightService;
    private final FitbitAiDailyAggregationService aiDailyAggregationService;
    private final UserRepository userRepository;


    public FitbitAiInsightScheduler(
            AiDailyInsightRepository insightRepository,
            AiInsightService aiInsightService,
            FitbitAiDailyAggregationService aiDailyAggregationService,
            UserRepository userRepository
    ) {
        this.insightRepository = insightRepository;
        this.aiInsightService = aiInsightService;
        this.aiDailyAggregationService = aiDailyAggregationService;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 22 * * ?") //10 PM daily:
    @Transactional
    public void generateDailyInsightForEligibleUsers() {
        LocalDate today = LocalDate.now();
        List<User> users = insightRepository.findUsersWithManualDailyLogsButNoInsights(today);

        if (users.isEmpty()) {
            log.info("No eligible users found. Exiting scheduler.");
            return;
        }

        int generatedCount = 0;

        for (int i = 0; i < users.size(); i++) {
            try {
                User readyUser = userRepository.findById(users.get(i).getId()).orElseThrow();

                readyUser.getUserInjuryList().size();
                readyUser.getUserSurgeryList().size();
                readyUser.getUserDiscIssueList().size();

                AiUserDailyInputDto dto = aiDailyAggregationService.buildAiInput(readyUser.getEmail(), today);
                aiInsightService.generateDailyInsight(dto, readyUser.getEmail(), today);
                log.info("Generated AI insight for user {} through scheduler cronjob:", readyUser.getPublicId());
                generatedCount++;
            } catch (Exception e) {
                log.error("Error generating AI insight for user {}", users.get(i).getPublicId(), e);
            }
        }
        log.info("AI Insight Scheduler completed. Insights generated = {}", generatedCount);
    }
}
