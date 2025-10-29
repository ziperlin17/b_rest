package com.example.bankrest.dto;
import com.example.bankrest.entities.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for updating a user's status")
public class UserStatusUpdateRequestDto {

    @Schema(description = "The new status for the user", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status cannot be null")
    private UserStatus newStatus;
}