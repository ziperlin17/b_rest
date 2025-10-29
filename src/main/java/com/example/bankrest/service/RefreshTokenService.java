package com.example.bankrest.service;

import com.example.bankrest.entities.RefreshToken;
import com.example.bankrest.entities.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
}
