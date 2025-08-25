package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private String fullName;
    private String email;
    private String profilePicture;
    private Long id;
}
