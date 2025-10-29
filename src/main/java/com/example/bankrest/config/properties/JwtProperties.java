package com.example.bankrest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("3600000")
        long accessTokenExpirationMs,
        @DefaultValue("86400000")
        long refreshTokenExpirationMs
) {
}