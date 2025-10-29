package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for user authentication")
public class LoginRequestDto {

    @Schema(description = "User's email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "User's password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password cannot be blank")
    private String password;
}