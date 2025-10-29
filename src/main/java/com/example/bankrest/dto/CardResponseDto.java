package com.example.bankrest.dto;
import com.example.bankrest.entities.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Response object containing public details of a bank card")
public record CardResponseDto(
        @Schema(description = "Unique identifier of the card")
        UUID uuid,

        @Schema(description = "Masked card number", example = "**** **** **** 1234")
        String maskedCardNumber,

        @Schema(description = "Name of the cardholder as it appears on the card", example = "JOHN DOE")
        String cardholderName,

        @Schema(description = "Card expiration date", example = "2029-12-31")
        LocalDate expirationDate,

        @Schema(description = "Current status of the card (e.g., ACTIVE, BLOCKED)")
        CardStatus status,

        @Schema(description = "The product name of the card", example = "Visa Classic Debit")
        String cardProductName,

        @Schema(description = "Identifier of the account to which the card is linked")
        UUID accountUuid,

        @Schema(description = "Current balance of the linked account")
        BigDecimal accountBalance
) {
}
