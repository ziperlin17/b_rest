package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO для отображения карточного продукта")
public class CardProductResponseDto {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Visa Classic")
    private String productName;

    @Schema(description = "Payment System", example = "VISA")
    private String paymentSystem;
}