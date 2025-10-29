package com.example.bankrest.dto;

import com.example.bankrest.entities.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO для обновления статуса карты")
public class CardStatusUpdateRequestDto {

    @Schema(description = "Новый статус для карты", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "New status cannot be null")
    private CardStatus newStatus;
}