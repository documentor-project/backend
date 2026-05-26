package com.documentor.backend.presentation.common;

public record ErrorResponse(
        String code,
        String message,
        Object details
) {

    public static ErrorResponse of(String code, String message, Object details) {
        return new ErrorResponse(code, message, details);
    }
}
