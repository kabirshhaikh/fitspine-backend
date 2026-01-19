package com.fitspine.controller;

import com.fitspine.service.WearableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/oauth")
public class OauthCallBackController {

    private final WearableService wearableService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OauthCallBackController(WearableService wearableService) {
        this.wearableService = wearableService;
    }

    @GetMapping("/callback")
    public ResponseEntity<String> fitBitCallBack(@RequestParam("code") String code, @RequestParam("state") Long userId) {
        wearableService.exchangeCodeForToken(code, userId);
        //Hardcoding this for now, later add env variable with prod url:
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendUrl + "/dashboard?fitbit=connected")
                .build();
    }

    @GetMapping("/fitbit/revoke")
    public ResponseEntity<Void> revokeFitbit() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        wearableService.revoke(email);
        return ResponseEntity.noContent().build();
    }
}
