package com.example.bankrest.dto;

import com.example.bankrest.entities.enums.UserStatus;
import lombok.Data;

import java.util.Set;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Set<String> roles;
}