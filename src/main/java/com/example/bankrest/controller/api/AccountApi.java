package com.example.bankrest.controller.api;

import com.example.bankrest.dto.AccountResponseDto;
import com.example.bankrest.dto.CreateAccountRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Account Management API", description = "APIs for creating and viewing bank accounts")
@RequestMapping("/v1/accounts")
@SecurityRequirement(name = "bearerAuth")
public interface AccountApi {

    @Operation(summary = "Get all accounts for the current user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of accounts")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<List<AccountResponseDto>> getCurrentUserAccounts(Authentication authentication);

    @Operation(summary = "Create a new account for the current user")
    @ApiResponse(responseCode = "201", description = "Account created successfully")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<AccountResponseDto> createAccount(Authentication authentication, @Valid @RequestBody CreateAccountRequestDto createAccountDto);

    @Operation(summary = "Get account details by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved account details"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You do not own this account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<AccountResponseDto> getAccountByUuid(
            Authentication authentication,
            @Parameter(description = "UUID account") @PathVariable UUID uuid
    );

    @Operation(summary = "Close (delete) an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account successfully closed"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Account balance is not zero or cards are still active"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You do not own this account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<Void> closeAccount(
            Authentication authentication,
            @Parameter(description = "UUID account for closing") @PathVariable UUID uuid
    );
}