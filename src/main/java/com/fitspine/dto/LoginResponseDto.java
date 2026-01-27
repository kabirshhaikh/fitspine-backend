package com.fitspine.dto;

import com.fitspine.enums.WearableType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private String fullName;
    private String email;
    private String profilePicture;
    private Boolean isWearableConnected;
    private WearableType wearableType;
    private boolean hasOnBoardingCompleted;
    private boolean needsProfileCompletion;
    private Long id;
}
