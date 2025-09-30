package com.fitspine.service;

import com.fitspine.dto.ManualDailyLogInputDto;
import com.fitspine.dto.ManualDailyLogPatchDto;
import com.fitspine.dto.ManualDailyLogResponseDto;
import com.fitspine.model.ManualDailyLog;

import java.time.LocalDate;

public interface ManualDailyLogService {
    ManualDailyLogResponseDto createDailyLog(String email, ManualDailyLogInputDto dto);

    ManualDailyLogResponseDto updateDailyLog(String email, ManualDailyLogPatchDto dto, Long id);

    ManualDailyLogResponseDto getLog(String email, LocalDate date);
}
