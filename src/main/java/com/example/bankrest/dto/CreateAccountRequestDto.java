package com.example.bankrest.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a new bank account")
public class CreateAccountRequestDto {

    @Schema(description = "ISO 4217 currency code for the new account.", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;
}
