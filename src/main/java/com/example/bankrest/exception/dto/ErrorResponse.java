package com.example.bankrest.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standardized error response object")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "Timestamp of when the error occurred")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "High-level error reason", example = "Not Found")
        String error,

        @Schema(description = "Specific error message", example = "User with id 5 not found")
        String message,

        @Schema(description = "The path of the request that caused the error", example = "/api/v1/users/5")
        String path,

        @Schema(description = "Validation errors, if any")
        Map<String, String> validationErrors
) {
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
}
