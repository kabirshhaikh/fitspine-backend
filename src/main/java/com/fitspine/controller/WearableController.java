package com.fitspine.controller;

import com.fitspine.service.WearableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/wearable")
public class WearableController {
    private final WearableService wearableService;

    public WearableController(WearableService wearableService) {
        this.wearableService = wearableService;
    }

    @GetMapping("/fitbit/auth-url")
    public ResponseEntity<String> getFitBitAuthUrl() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        String authUrl = wearableService.buildAuthUrl(email);
        return ResponseEntity.ok(authUrl);
    }
}
