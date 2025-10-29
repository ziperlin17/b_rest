package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO for creating Card Product")
public class CreateCardProductRequestDto {

    @Schema(description = "Product name", example = "Visa Gold", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255)
    private String productName;

    @Schema(description = "Payment system", example = "VISA", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Payment system cannot be blank")
    @Size(max = 100)
    private String paymentSystem;
}