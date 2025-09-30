package com.fitspine.service.impl;

import com.fitspine.dto.ManualDailyLogInputDto;
import com.fitspine.dto.ManualDailyLogPatchDto;
import com.fitspine.dto.ManualDailyLogResponseDto;
import com.fitspine.enums.PainLocation;
import com.fitspine.exception.ResourceNotFoundException;
import com.fitspine.exception.UserMismatchException;
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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Transactional
    @Override
    public ManualDailyLogResponseDto updateDailyLog(String email, ManualDailyLogPatchDto dto, Long id) {
        //Get authenticated user:
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        //Get log:
        ManualDailyLog log = manualDailyLogRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Manual daily log with id:" + id + " not found"));

        //Check ownership:
        if (!log.getUser().getId().equals(user.getId())) {
            throw new UserMismatchException("You are not authorized to update this log");
        }

        //Update:
        if (dto.getPainLevel() != null) {
            log.setPainLevel(dto.getPainLevel());
        }

        if (dto.getFlareUpToday() != null) {
            log.setFlareUpToday(dto.getFlareUpToday());
        }

        if (dto.getNumbnessTingling() != null) {
            log.setNumbnessTingling(dto.getNumbnessTingling());
        }

        if (dto.getSittingTime() != null) {
            log.setSittingTime(dto.getSittingTime());
        }

        if (dto.getStandingTime() != null) {
            log.setStandingTime(dto.getStandingTime());
        }

        if (dto.getStretchingDone() != null) {
            log.setStretchingDone(dto.getStretchingDone());
        }

        if (dto.getMorningStiffness() != null) {
            log.setMorningStiffness(dto.getMorningStiffness());
        }

        if (dto.getStressLevel() != null) {
            log.setStressLevel(dto.getStressLevel());
        }

        if (dto.getLiftingOrStrain() != null) {
            log.setLiftingOrStrain(dto.getLiftingOrStrain());
        }

        if (dto.getNotes() != null) {
            log.setNotes(dto.getNotes());
        }

        if (dto.getPainLocations() != null) {
            log.getManualDailyPainLocationLogs().clear();
            List<PainLocation> locations = dto.getPainLocations();
            Set<PainLocation> seen = new HashSet<>();

            for (int i = 0; i < locations.size(); i++) {
                ManualDailyPainLocationLog painLocationLog = new ManualDailyPainLocationLog();
                PainLocation location = locations.get(i);

                if (location == null || seen.contains(location)) {
                    continue;
                }

                painLocationLog.setManualDailyLog(log);
                painLocationLog.setPainLocation(location);
                log.getManualDailyPainLocationLogs().add(painLocationLog);

                seen.add(location);
            }
        }

        //Save log:
        ManualDailyLog updateLog = manualDailyLogRepository.save(log);


        //Map entity to dto:
        return ManualDailyLogResponseDto.builder()
                .id(log.getId())
                .logDate(log.getLogDate())
                .painLevel(log.getPainLevel())
                .flareUpToday(log.getFlareUpToday())
                .numbnessTingling(log.getNumbnessTingling())
                .sittingTime(log.getSittingTime())
                .standingTime(log.getStandingTime())
                .stretchingDone(log.getStretchingDone())
                .morningStiffness(log.getMorningStiffness())
                .stressLevel(log.getStressLevel())
                .liftingOrStrain(log.getLiftingOrStrain())
                .notes(log.getNotes())
                .painLocations(
                        log.getManualDailyPainLocationLogs().stream()
                                .map(ManualDailyPainLocationLog::getPainLocation)
                                .toList()
                )
                .build();
    }

    @Transactional
    @Override
    public ManualDailyLogResponseDto getLog(String email, LocalDate date) {
        //Find user:
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        //Find log:
        ManualDailyLog log = manualDailyLogRepository.findByUserAndLogDate(user, date).orElseThrow(() -> new ResourceNotFoundException("Log for date: " + date + " not found"));

        return ManualDailyLogResponseDto.builder()
                .id(log.getId())
                .logDate(log.getLogDate())
                .painLevel(log.getPainLevel())
                .flareUpToday(log.getFlareUpToday())
                .numbnessTingling(log.getNumbnessTingling())
                .sittingTime(log.getSittingTime())
                .standingTime(log.getStandingTime())
                .stretchingDone(log.getStretchingDone())
                .morningStiffness(log.getMorningStiffness())
                .stressLevel(log.getStressLevel())
                .liftingOrStrain(log.getLiftingOrStrain())
                .notes(log.getNotes())
                .painLocations(
                        log.getManualDailyPainLocationLogs().stream()
                                .map(ManualDailyPainLocationLog::getPainLocation)
                                .toList()
                )
                .build();
    }
}
