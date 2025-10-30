package com.fitspine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponseDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String securityToken;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
