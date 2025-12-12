package com.fitspine.service;

import java.time.LocalDate;

public interface AiInsightWorkerService {
    void processUserForCronjob(Long userId, LocalDate date);
}
