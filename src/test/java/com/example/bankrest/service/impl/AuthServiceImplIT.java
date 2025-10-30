package com.example.bankrest.service.impl;

import com.example.bankrest.dto.LoginRequestDto;
import com.example.bankrest.dto.LoginResponseDto;
import com.example.bankrest.dto.UserRegistrationDto;
import com.example.bankrest.entities.RefreshToken;
import com.example.bankrest.exception.AuthException;
import com.example.bankrest.repositories.RefreshTokenRepository;
import com.example.bankrest.repositories.UserRepository;
import com.example.bankrest.security.JwtTokenProvider;
import com.example.bankrest.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("Integration Tests for AuthService")
class AuthServiceImplIT {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.encryption.aes.key", () -> "12345678901234567890123456789012");
        registry.add("app.security.jwt.secret", () -> "test-jwt-secret-key-that-is-long-enough-for-hs256-algorithm");
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final String testUserEmail = "auth.user@example.com";
    private final String testUserPassword = "password123";

    @BeforeEach
    void setUp() {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setEmail(testUserEmail);
        registrationDto.setPassword(testUserPassword);
        registrationDto.setFirstName("Auth");
        registrationDto.setLastName("User");
        registrationDto.setPhoneNumber("555-1234");
        authService.register(registrationDto);
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Login should succeed with valid credentials and return valid tokens")
    void login_withValidCredentials_shouldReturnTokens() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(testUserEmail);
        loginRequest.setPassword(testUserPassword);
        LoginResponseDto response = authService.login(loginRequest);

        assertNotNull(response);
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertTrue(jwtTokenProvider.validateToken(response.accessToken()), "Access token should be valid");
        assertTrue(refreshTokenRepository.findByToken(response.refreshToken()).isPresent(), "Refresh token should be saved in the database");
    }

    @Test
    @DisplayName("Login should fail with invalid credentials")
    void login_withInvalidCredentials_shouldThrowBadCredentialsException() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(testUserEmail);
        loginRequest.setPassword("wrong-password"); // Use an incorrect password
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Refresh token should succeed with a valid refresh token and return new tokens")
    @Transactional
    void refreshToken_withValidToken_shouldReturnNewTokens() throws InterruptedException {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(testUserEmail);
        loginRequest.setPassword(testUserPassword);
        LoginResponseDto initialTokens = authService.login(loginRequest);
        Thread.sleep(5);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + initialTokens.refreshToken());
        LoginResponseDto newTokens = authService.refreshToken(request);

        assertNotNull(newTokens);
        assertThat(newTokens.accessToken()).isNotBlank().isNotEqualTo(initialTokens.accessToken());
        assertThat(newTokens.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("Refresh token should fail with an expired refresh token")
    @Transactional
    void refreshToken_withExpiredToken_shouldThrowAuthException() {
        var user = userRepository.findByEmail(testUserEmail).orElseThrow();
        RefreshToken expiredToken = new RefreshToken(null, user, UUID.randomUUID().toString(), Instant.now().minusSeconds(1000));
        refreshTokenRepository.save(expiredToken);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken.getToken());

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals("Refresh token was expired. Please make a new sign-in request.", exception.getMessage());
    }

    @Test
    @DisplayName("Refresh token should fail if token is not found in database")
    void refreshToken_withNonExistentToken_shouldThrowAuthException() {
        String nonExistentToken = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + nonExistentToken);

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals("Refresh token not found in database.", exception.getMessage());
    }
}