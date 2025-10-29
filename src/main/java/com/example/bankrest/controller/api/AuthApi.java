package com.example.bankrest.controller.api;

import com.example.bankrest.dto.LoginRequestDto;
import com.example.bankrest.dto.LoginResponseDto;
import com.example.bankrest.dto.UserRegistrationDto;
import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Authentication API", description = "APIs for user registration and login")
@RequestMapping("/api/v1/auth")
public interface AuthApi {

    @Operation(summary = "Register a new user",
            description = "Creates a new user account with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User with this email or phone number already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })


    @PostMapping("/register")
    ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto);

    @Operation(summary = "Authenticate user and get tokens",
            description = "Provides JWT access and refresh tokens upon successful authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto);

    @Operation(summary = "Refresh access token",
            description = "Issues a new pair of access and refresh tokens using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    ResponseEntity<LoginResponseDto> refreshToken(
            @Parameter(description = "The Authorization header containing the Bearer refresh token", required = true)
            HttpServletRequest request
    );
}