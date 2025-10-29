package com.example.bankrest.service;

import com.example.bankrest.dto.LoginRequestDto;
import com.example.bankrest.dto.LoginResponseDto;
import com.example.bankrest.dto.UserRegistrationDto;
import com.example.bankrest.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    UserResponseDto register(UserRegistrationDto registrationDto);
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    LoginResponseDto refreshToken(HttpServletRequest request);

}