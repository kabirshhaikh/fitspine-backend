package com.fitspine.dto;

import com.fitspine.enums.Gender;
import com.fitspine.enums.WearableType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private Integer age;
    private Gender gender;
    private String profilePicture;
    private Boolean isResearchOpt;
    private Boolean isWearableConnected;
    private WearableType wearableType;

    private List<UserInjuryDto> userInjuries;
    private List<UserSurgeryDto> userSurgeries;
    private List<UserDiscIssueDto> userDiscIssues;
}
