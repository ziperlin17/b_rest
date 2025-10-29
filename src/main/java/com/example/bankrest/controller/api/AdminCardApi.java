package com.example.bankrest.controller.api;

import com.example.bankrest.dto.CardResponseDto;
import com.example.bankrest.dto.CardStatusUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin: Card Management API", description = "APIs for administrators to manage any card")
@RequestMapping("/v1/admin/cards")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public interface AdminCardApi {

    @Operation(summary = "Get all cards in the system",
            description = "Retrieves a paginated list of all cards, optionally filtered by user ID.")
    @GetMapping
    ResponseEntity<Page<CardResponseDto>> getAllCards(
            @Parameter(description = "Optional user ID to filter cards by")
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 20) Pageable pageable
    );

    @Operation(summary = "Delete a card",
            description = "Permanently deletes a card from the system. This is a destructive operation.")
    @DeleteMapping("/{uuid}")
    ResponseEntity<Void> deleteCard(
            @Parameter(description = "UUID of the card to delete") @PathVariable("uuid") UUID cardUuid
    );

    @Operation(summary = "Update a card's status",
            description = "Allows an admin to change the status of any card (e.g., ACTIVE, BLOCKED).")
    @PatchMapping("/{uuid}/status")
    ResponseEntity<CardResponseDto> updateCardStatus(
            @Parameter(description = "UUID карты для обновления") @PathVariable("uuid") UUID cardUuid,
            @Valid @RequestBody CardStatusUpdateRequestDto statusUpdateDto
    );
}
