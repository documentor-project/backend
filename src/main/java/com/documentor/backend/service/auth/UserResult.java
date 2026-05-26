package com.documentor.backend.service.auth;

import com.documentor.backend.domain.user.User;
import java.time.LocalDateTime;

public record UserResult(
        Long userId,
        String email,
        String nickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static UserResult from(User user) {
        return new UserResult(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
