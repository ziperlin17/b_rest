package com.example.bankrest.controller.api;

import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User Management API", description = "APIs for managing user profiles")
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
public interface UserApi {

    @Operation(summary = "Get current user's profile",
            description = "Retrieves the profile information for the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<UserResponseDto> getMyProfile(Authentication authentication);

    @Operation(summary = "Get user by ID (Admin only)",
            description = "Retrieves a specific user's profile by their ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") Long id);

    @Operation(summary = "Get all users (Admin only)",
            description = "Retrieves a paginated list of all users. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable);
}