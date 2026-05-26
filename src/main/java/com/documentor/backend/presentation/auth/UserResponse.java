package com.documentor.backend.presentation.auth;

import com.documentor.backend.service.auth.UserResult;
import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String email,
        String nickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static UserResponse from(UserResult result) {
        return new UserResponse(
                result.userId(),
                result.email(),
                result.nickname(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
