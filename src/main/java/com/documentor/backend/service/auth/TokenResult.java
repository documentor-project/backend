package com.documentor.backend.service.auth;

public record TokenResult(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
