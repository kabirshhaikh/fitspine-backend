package com.fitspine.service;

import com.fitspine.dto.FeedbackRequestDto;

public interface FeedbackService {
    void submitFeedback(FeedbackRequestDto dto);
}
