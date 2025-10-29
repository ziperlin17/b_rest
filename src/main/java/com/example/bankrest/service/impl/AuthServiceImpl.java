package com.example.bankrest.service.impl;

import com.example.bankrest.dto.LoginRequestDto;
import com.example.bankrest.dto.LoginResponseDto;
import com.example.bankrest.dto.UserRegistrationDto;
import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.entities.RefreshToken;
import com.example.bankrest.exception.AuthException;
import com.example.bankrest.security.JwtTokenProvider;
import com.example.bankrest.security.UserDetailsImpl;
import com.example.bankrest.service.AuthService;
import com.example.bankrest.service.RefreshTokenService;
import com.example.bankrest.service.UserService;
import com.example.bankrest.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    @Override
    public UserResponseDto register(UserRegistrationDto registrationDto) {
        return userService.createUser(registrationDto);
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.user());

        log.info("IN login - user with email: {} successfully logged in", userDetails.getUsername());

        return new LoginResponseDto(accessToken, refreshToken.getToken());
    }


    @Override
    @Transactional
    public LoginResponseDto refreshToken(HttpServletRequest request) {
        String requestRefreshToken = JwtUtils.extractToken(request)
                .orElseThrow(() -> new AuthException("Refresh token is missing."));

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetailsImpl userDetails = new UserDetailsImpl(user);
                    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
                    log.info("IN refreshToken - Tokens for user {} successfully refreshed", user.getEmail());
                    return new LoginResponseDto(newAccessToken, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new AuthException("Refresh token not found in database."));
    }
}