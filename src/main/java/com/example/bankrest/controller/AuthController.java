package com.example.bankrest.controller;

import com.example.bankrest.controller.api.AuthApi;
import com.example.bankrest.dto.LoginRequestDto;
import com.example.bankrest.dto.LoginResponseDto;
import com.example.bankrest.dto.UserRegistrationDto;
import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<UserResponseDto> register(UserRegistrationDto registrationDto) {
        UserResponseDto createdUser = authService.register(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto loginRequestDto) {
        LoginResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LoginResponseDto> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
