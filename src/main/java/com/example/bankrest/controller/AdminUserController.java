package com.example.bankrest.controller;

import com.example.bankrest.controller.api.AdminUserApi;
import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.dto.UserStatusUpdateRequestDto;
import com.example.bankrest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminUserController implements AdminUserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }
    @Override
    public ResponseEntity<UserResponseDto> updateUserStatus(Long userId, UserStatusUpdateRequestDto statusUpdateDto) {
        UserResponseDto updatedUser = userService.updateUserStatus(userId, statusUpdateDto.getNewStatus());
        return ResponseEntity.ok(updatedUser);
    }
}