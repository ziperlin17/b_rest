package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(description = "Request object for transferring funds between accounts")
public class TransferRequestDto {

    @Schema(description = "UUID of the source account from which funds will be withdrawn. Must belong to the authenticated user.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Source account UUID cannot be null")
    private UUID fromAccountUuid;

    @Schema(description = "Account number of the destination account. Can be any valid account in the system.", example = "ACC123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Destination account number cannot be blank")
    private String toAccountNumber;

    @Schema(description = "The amount to transfer. Must be a positive value.", example = "100.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Transfer amount must be positive")
    private BigDecimal amount;

    @Schema(description = "ISO 4217 currency code of the transfer.", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    @Schema(description = "Optional description or purpose of the transfer.", example = "Payment for services")
    private String description;
}