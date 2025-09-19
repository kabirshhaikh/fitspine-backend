package com.fitspine.controller;

import com.fitspine.service.WearableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/oauth")
public class OauthCallBackController {

    private final WearableService wearableService;

    public OauthCallBackController(WearableService wearableService) {
        this.wearableService = wearableService;
    }

    @GetMapping("/callback")
    public ResponseEntity<String> fitBitCallBack(@RequestParam("code") String code, @RequestParam("state") Long userId) {
        wearableService.exchangeCodeForToken(code, userId);
        return ResponseEntity.ok("Fitbit Connected Successfully");
    }
}
