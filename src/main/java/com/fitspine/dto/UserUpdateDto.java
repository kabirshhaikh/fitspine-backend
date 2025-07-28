package com.fitspine.dto;

import com.fitspine.enums.Gender;
import com.fitspine.enums.WearableType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UserUpdateDto {
    private Integer age;
    private Gender gender;
    private MultipartFile profilePicture;
    private Boolean surgeryHistory;
    private Boolean isResearchOpt;
    private Boolean isWearableConnected;
    private WearableType wearableType;

    private List<UserInjuryDto> userInjuries;
    private List<UserSurgeryDto> userSurgeries;
    private List<UserDiscIssueDto> userDiscIssues;
}
