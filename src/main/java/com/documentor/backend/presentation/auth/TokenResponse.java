package com.documentor.backend.presentation.auth;

import com.documentor.backend.service.auth.TokenResult;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {

    public static TokenResponse from(TokenResult result) {
        return new TokenResponse(
                result.accessToken(),
                result.refreshToken(),
                "Bearer",
                result.expiresIn()
        );
    }
}
