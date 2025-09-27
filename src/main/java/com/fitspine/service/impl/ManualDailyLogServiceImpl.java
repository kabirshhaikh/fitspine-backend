package com.fitspine.service.impl;

import com.fitspine.dto.ManualDailyLogInputDto;
import com.fitspine.dto.ManualDailyLogResponseDto;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.ManualDailyLog;
import com.fitspine.model.ManualDailyPainLocationLog;
import com.fitspine.model.User;
import com.fitspine.repository.ManualDailyLogRepository;
import com.fitspine.repository.ManualDailyPainLocationLogRepository;
import com.fitspine.repository.UserRepository;
import com.fitspine.service.ManualDailyLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManualDailyLogServiceImpl implements ManualDailyLogService {
    private final ManualDailyLogRepository manualDailyLogRepository;
    private final ManualDailyPainLocationLogRepository painLocationLogRepository;
    private final UserRepository userRepository;

    public ManualDailyLogServiceImpl(ManualDailyLogRepository manualDailyLogRepository, ManualDailyPainLocationLogRepository painLocationLogRepository, UserRepository userRepository) {
        this.manualDailyLogRepository = manualDailyLogRepository;
        this.painLocationLogRepository = painLocationLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public ManualDailyLogResponseDto createDailyLog(String email, ManualDailyLogInputDto dto) {
        //Get user:
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        //Check for duplicate entry:
        if (manualDailyLogRepository.existsByUserAndLogDate(user, dto.getLogDate())) {
            throw new RuntimeException("Log already exists for date:" + dto.getLogDate());
        }

        //Map input dto to model:
        ManualDailyLog savedLog = ManualDailyLog.builder()
                .user(user)
                .logDate(dto.getLogDate())
                .painLevel(dto.getPainLevel())
                .flareUpToday(dto.getFlareUpToday())
                .numbnessTingling(dto.getNumbnessTingling())
                .sittingTime(dto.getSittingTime())
                .standingTime(dto.getStandingTime())
                .stretchingDone(dto.getStretchingDone())
                .morningStiffness(dto.getMorningStiffness())
                .stressLevel(dto.getStressLevel())
                .liftingOrStrain(dto.getLiftingOrStrain())
                .notes(dto.getNotes())
                .build();

        //Save parent log:
        manualDailyLogRepository.save(savedLog);

        //Save child pain location log:
        if (dto.getPainLocations() != null) {
            dto.getPainLocations().forEach(location -> {
                ManualDailyPainLocationLog painLog = ManualDailyPainLocationLog.builder()
                        .manualDailyLog(savedLog)
                        .painLocation(location)
                        .build();
                painLocationLogRepository.save(painLog);
                savedLog.getManualDailyPainLocationLogs().add(painLog);
            });
        }

        //Entity to response dto:
        return ManualDailyLogResponseDto.builder()
                .id(savedLog.getId())
                .logDate(savedLog.getLogDate())
                .painLevel(savedLog.getPainLevel())
                .flareUpToday(savedLog.getFlareUpToday())
                .numbnessTingling(savedLog.getNumbnessTingling())
                .sittingTime(savedLog.getSittingTime())
                .standingTime(savedLog.getStandingTime())
                .stretchingDone(savedLog.getStretchingDone())
                .morningStiffness(savedLog.getMorningStiffness())
                .stressLevel(savedLog.getStressLevel())
                .liftingOrStrain(savedLog.getLiftingOrStrain())
                .notes(savedLog.getNotes())
                .painLocations(
                        savedLog.getManualDailyPainLocationLogs().stream()
                                .map(ManualDailyPainLocationLog::getPainLocation)
                                .toList()
                )
                .build();
    }

    @Override
    public void updateDailyLog(String email) {
        //TODO
    }
}
