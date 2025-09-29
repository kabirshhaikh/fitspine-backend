package com.fitspine.controller;

import com.fitspine.dto.ManualDailyLogInputDto;
import com.fitspine.dto.ManualDailyLogPatchDto;
import com.fitspine.dto.ManualDailyLogResponseDto;
import com.fitspine.service.ManualDailyLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/manual-daily-log")
public class ManualDailyLogController {
    private final ManualDailyLogService manualDailyLogService;

    public ManualDailyLogController(ManualDailyLogService manualDailyLogService) {
        this.manualDailyLogService = manualDailyLogService;
    }

    @PostMapping
    public ResponseEntity<ManualDailyLogResponseDto> createDailyLog(@RequestBody ManualDailyLogInputDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        ManualDailyLogResponseDto response = manualDailyLogService.createDailyLog(email, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}") // this is manual log id
    public ResponseEntity<ManualDailyLogResponseDto> updateDailyLog(@RequestBody ManualDailyLogPatchDto dto, @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        ManualDailyLogResponseDto response = manualDailyLogService.updateDailyLog(email, dto, id);
        return ResponseEntity.ok(response);
    }
}
