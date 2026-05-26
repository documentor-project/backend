package com.documentor.backend.presentation.share;

import java.time.LocalDateTime;

public record CreateShareLinkRequest(
        LocalDateTime expiresAt
) {
}
