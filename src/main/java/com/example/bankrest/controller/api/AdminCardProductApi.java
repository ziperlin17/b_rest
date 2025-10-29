package com.example.bankrest.controller.api;

import com.example.bankrest.dto.CardProductResponseDto;
import com.example.bankrest.dto.CreateCardProductRequestDto;
import com.example.bankrest.dto.UpdateCardProductRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin: Card Product Management", description = "API для управления карточными продуктами")
@RequestMapping("/v1/admin/card-products")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public interface AdminCardProductApi {

    @Operation(summary = "Get all card products")
    @GetMapping
    ResponseEntity<List<CardProductResponseDto>> getAllCardProducts();

    @Operation(summary = "Create new card product")
    @PostMapping
    ResponseEntity<CardProductResponseDto> createCardProduct(
            @Valid @RequestBody CreateCardProductRequestDto createDto
    );

    @Operation(summary = "Update card product")
    @PatchMapping("/{id}")
    ResponseEntity<CardProductResponseDto> updateCardProduct(
            @Parameter(description = "ID продукта для обновления") @PathVariable Long id,
            @Valid @RequestBody UpdateCardProductRequestDto updateDto
    );
}
