package com.fitspine.service.impl;

import com.fitspine.dto.FeedbackRequestDto;
import com.fitspine.exception.InvalidFeedbackException;
import com.fitspine.model.Feedback;
import com.fitspine.repository.FeedbackRepository;
import com.fitspine.service.FeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository repository;

    public FeedbackServiceImpl(FeedbackRepository repository) {
        this.repository = repository;
    }

    @Override
    public void submitFeedback(FeedbackRequestDto dto) {
        if (Boolean.TRUE.equals(dto.getContactRequested()) && dto.getEmail() == null) {
            throw new InvalidFeedbackException(
                    "Email is required when contact is requested"
            );
        }

        Feedback feedback = Feedback.builder()
                .feedbackType(dto.getFeedbackType())
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .rating(dto.getRating())
                .contactRequested(Boolean.TRUE.equals(dto.getContactRequested()))
                .email(dto.getEmail())
                .build();

        repository.save(feedback);
    }
}
