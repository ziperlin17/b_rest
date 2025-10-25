package com.example.bankrest.service;

import com.example.bankrest.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User createUser(String username, String rawPassword);
    User findByUsername(String username);

}