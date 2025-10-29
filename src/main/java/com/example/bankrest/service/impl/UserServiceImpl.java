package com.example.bankrest.service.impl;

import com.example.bankrest.dto.UserRegistrationDto;
import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.entities.Role;
import com.example.bankrest.entities.User;
import com.example.bankrest.entities.enums.RoleType;
import com.example.bankrest.entities.enums.UserStatus;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.exception.UserAlreadyExistsException;
import com.example.bankrest.dto.mapper.UserMapper;
import com.example.bankrest.repositories.RoleRepository;
import com.example.bankrest.repositories.UserRepository;
import com.example.bankrest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto createUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + registrationDto.getEmail() + " already exists.");
        }
        if (userRepository.existsByPhoneNumber(registrationDto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("User with phone number " + registrationDto.getPhoneNumber() + " already exists.");
        }

        User user = userMapper.toUser(registrationDto);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER not found. Ensure it is seeded in the database."));
        user.setRoles(Set.of(userRole));
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        log.info("IN createUser - user: {} successfully created", savedUser.getEmail());

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserProfileById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toUserResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        log.info("IN getAllUsers - successfully retrieved {} users.", userPage.getTotalElements());
        return userPage.map(userMapper::toUserResponseDto);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserStatus(Long userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus(newStatus);
        User updatedUser = userRepository.save(user);

        log.info("IN updateUserStatus - Status of user {} changed to {}", user.getEmail(), newStatus);
        return userMapper.toUserResponseDto(updatedUser);
    }
}