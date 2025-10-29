package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Request object for creating a new bank card")
public class CreateCardRequestDto {

    @Schema(description = "UUID of the account to which the new card will be linked. The account must belong to the authenticated user.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Account UUID cannot be null")
    private UUID accountUuid;

    @Schema(description = "ID of the card product to be issued (e.g., 1 for 'Visa Classic Debit').", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Card product ID cannot be null")
    private Long cardProductId;

    @Schema(description = "Name of the cardholder. Must match the account owner's name format.", example = "JOHN DOE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Cardholder name cannot be blank")
    @Size(min = 3, max = 255, message = "Cardholder name must be between 3 and 255 characters")
    private String cardholderName;
}