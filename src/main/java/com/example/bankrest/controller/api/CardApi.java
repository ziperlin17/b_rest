package com.example.bankrest.controller.api;

import com.example.bankrest.dto.CardResponseDto;
import com.example.bankrest.dto.CreateCardRequestDto;
import com.example.bankrest.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Card Management API", description = "APIs for creating, viewing, and managing bank cards")
@RequestMapping("/v1/cards")
@SecurityRequirement(name = "bearerAuth")
public interface CardApi {

    @Operation(summary = "Get all cards for the current user",
            description = "Retrieves a paginated list of all cards belonging to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of cards"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<Page<CardResponseDto>> getCurrentUserCards(
            Authentication authentication,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    );

    @Operation(summary = "Create a new card for the current user",
            description = "Issues a new card for one of the user's accounts based on a selected card product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User tries to create a card for an account they do not own"),
            @ApiResponse(responseCode = "404", description = "Account or Card Product not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<CardResponseDto> createCard(
            Authentication authentication,
            @Valid @RequestBody CreateCardRequestDto createCardRequestDto
    );

    @Operation(summary = "Block a card",
            description = "Allows a user to block one of their own cards, or an admin to block any card.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card blocked successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User tries to block a card they do not own"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PatchMapping("/{uuid}/block")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ResponseEntity<Void> blockCard(
            Authentication authentication,
            @Parameter(description = "UUID of the card to be blocked") @PathVariable("uuid") UUID cardUuid
    );
}