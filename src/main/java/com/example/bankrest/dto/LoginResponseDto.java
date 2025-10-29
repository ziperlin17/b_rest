package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing JWT tokens")
public record LoginResponseDto(
        @Schema(description = "JWT Access Token for authenticating requests. Short-lived.")
        String accessToken,

        @Schema(description = "JWT Refresh Token for obtaining a new access token. Long-lived.")
        String refreshToken
) {
}