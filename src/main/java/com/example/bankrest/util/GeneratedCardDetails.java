package com.example.bankrest.util;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Immutable data carrier for new generated card details")
public record GeneratedCardDetails(
        @Schema(description = "Unique 16-digit card number")
        String cardNumber,

        @Schema(description = "Random CVV")
        String cvv,

        @Schema(description = "The card's expiration date")
        LocalDate expirationDate
) {
}