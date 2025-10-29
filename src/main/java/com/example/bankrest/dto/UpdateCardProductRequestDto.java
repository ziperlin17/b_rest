package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Card product update DTO")
public class UpdateCardProductRequestDto {

    @Schema(description = "New card product name", example = "Visa Gold Plus")
    @Size(max = 255)
    private String productName;

    @Schema(description = "New payment system", example = "VISA")
    @Size(max = 100)
    private String paymentSystem;
}