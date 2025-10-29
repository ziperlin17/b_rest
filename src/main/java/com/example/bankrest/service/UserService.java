package com.example.bankrest.service;

import com.example.bankrest.dto.UserRegistrationDto;
import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.entities.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDto createUser(UserRegistrationDto registrationDto);
    UserResponseDto getUserProfileById(Long id);
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    UserResponseDto updateUserStatus(Long userId, UserStatus newStatus);
}