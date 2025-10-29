package com.example.bankrest.controller;

import com.example.bankrest.controller.api.UserApi;
import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.security.UserDetailsImpl;
import com.example.bankrest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponseDto> getMyProfile(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserResponseDto profile = userService.getUserProfileById(userDetails.user().getId());
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<UserResponseDto> getUserById(Long id) {
        UserResponseDto user = userService.getUserProfileById(id);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
}
