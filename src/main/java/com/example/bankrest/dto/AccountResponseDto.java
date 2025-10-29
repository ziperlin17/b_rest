package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Schema(description = "Response object containing public details of a bank account")
public record AccountResponseDto(
        @Schema(description = "Unique identifier of the account")
        UUID uuid,

        @Schema(description = "The account's public number", example = "ACC1234567890")
        String accountNumber,

        @Schema(description = "Current balance of the account")
        BigDecimal balance,

        @Schema(description = "ISO 4217 currency code of the account", example = "USD")
        Currency currency
) {
}

