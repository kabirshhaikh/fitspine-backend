package com.fitspine.scheduler;

import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.model.User;
import com.fitspine.repository.AiDailyInsightRepository;
import com.fitspine.repository.UserRepository;
import com.fitspine.service.AiInsightService;
import com.fitspine.service.AiInsightWorkerService;
import com.fitspine.service.FitbitAiDailyAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class FitbitAiInsightScheduler {
    private final UserRepository userRepository;
    private final AiInsightWorkerService workerService;
    private final AiDailyInsightRepository insightRepository;


    public FitbitAiInsightScheduler(
            AiDailyInsightRepository insightRepository,
            AiInsightWorkerService workerService,
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
        this.workerService = workerService;
        this.insightRepository = insightRepository;
    }

    //    @Scheduled(cron = "0 0 22 * * ?") //10 PM daily:
    @Scheduled(cron = "0 57 23 * * ?")
    public void generateDailyInsightForEligibleUsers() {
        LocalDate today = LocalDate.now();
        List<Long> ids = insightRepository.findUsersWithManualDailyLogsButNoInsights(today);
        log.info("Length of the ids array: {}", ids.size());

        if (ids.isEmpty()) {
            log.info("No eligible users found. Exiting scheduler.");
            return;
        }

        int generatedCount = 0;
        for (int i = 0; i < ids.size(); i++) {
            workerService.processUserForCronjob(ids.get(i), today);
            generatedCount++;
        }
        log.info("AI Insight Scheduler completed. Insights generated = {}", generatedCount);
    }
}
