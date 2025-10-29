package com.example.bankrest.controller.api;

import com.example.bankrest.dto.TransferRequestDto;
import com.example.bankrest.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Transaction API", description = "APIs for performing financial transactions")
@RequestMapping("/v1/transactions")
@SecurityRequirement(name = "bearerAuth")
public interface TransactionApi {

    @Operation(summary = "Transfer funds between accounts",
            description = "Allows an authenticated user to transfer a specified amount from one of their accounts to any other account in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction completed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data, insufficient funds, or currency mismatch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User tries to transfer funds from an account they do not own"),
            @ApiResponse(responseCode = "404", description = "Source or destination account not found")
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<Void> transferFunds(
            Authentication authentication,
            @Valid @RequestBody TransferRequestDto transferRequestDto
    );
}
