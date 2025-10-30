package com.example.bankrest.service.impl;
import com.example.bankrest.config.properties.JwtProperties;
import com.example.bankrest.entities.RefreshToken;
import com.example.bankrest.entities.User;
import com.example.bankrest.exception.AuthException;
import com.example.bankrest.repositories.RefreshTokenRepository;
import com.example.bankrest.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUser(user);

        RefreshToken refreshToken;
        if (existingTokenOpt.isPresent()) {
            refreshToken = existingTokenOpt.get();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()));
            log.debug("IN createRefreshToken - Updating existing token for user {}", user.getEmail());
        } else {
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()))
                    .build();
            log.debug("IN createRefreshToken - Creating new token for user {}", user.getEmail());
        }
        return refreshTokenRepository.save(refreshToken);
    }
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByTokenWithUser(token);
    }
    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new AuthException("Refresh token was expired. Please make a new sign-in request.");
        }
        return token;
    }
}
