package com.fitspine.service;

import com.fitspine.dto.ManualDailyLogInputDto;
import com.fitspine.dto.ManualDailyLogResponseDto;
import com.fitspine.model.ManualDailyLog;

public interface ManualDailyLogService {
    ManualDailyLogResponseDto createDailyLog(String email, ManualDailyLogInputDto dto);

    void updateDailyLog(String email);
}
