package com.example.bankrest.controller.api;

import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.dto.UserStatusUpdateRequestDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin: User Management API", description = "APIs for administrators to manage users")
@RequestMapping("/v1/admin/users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public interface AdminUserApi {

    @Operation(summary = "Get all users",
            description = "Retrieves a paginated list of all users in the system.")
    @GetMapping
    ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    );

    @Operation(summary = "Update a user's status",
            description = "Allows an admin to change the status of a user (e.g., ACTIVE, SUSPENDED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status provided"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/status")
    ResponseEntity<UserResponseDto> updateUserStatus(
            @Parameter(description = "ID of the user to update") @PathVariable("id") Long userId,
            @Valid @RequestBody UserStatusUpdateRequestDto statusUpdateDto
    );
}