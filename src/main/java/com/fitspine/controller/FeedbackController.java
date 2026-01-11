package com.fitspine.controller;

import com.fitspine.dto.FeedbackRequestDto;
import com.fitspine.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<String> submitFeedback(
            @Valid @RequestBody FeedbackRequestDto dto
    ) {
        service.submitFeedback(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Feedback submitted successfully");
    }
}
