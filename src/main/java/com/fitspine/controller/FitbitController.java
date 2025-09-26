package com.fitspine.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitspine.service.FitbitApiClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/wearable/fitbit")
public class FitbitController {
    private final FitbitApiClientService fitbitApiClientService;

    public FitbitController(FitbitApiClientService fitbitApiClientService) {
        this.fitbitApiClientService = fitbitApiClientService;
    }

    @GetMapping("/activity")
    public ResponseEntity<JsonNode> getActivity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return ResponseEntity.ok(fitbitApiClientService.getActivity(email, date));
    }

    @GetMapping("/sleep")
    public ResponseEntity<JsonNode> getSleep() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return ResponseEntity.ok(fitbitApiClientService.getSleep(email, date));
    }

    @GetMapping("/heart-rate")
    public ResponseEntity<JsonNode> getHeartRate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return ResponseEntity.ok(fitbitApiClientService.getHeartRate(email, date));
    }
}
