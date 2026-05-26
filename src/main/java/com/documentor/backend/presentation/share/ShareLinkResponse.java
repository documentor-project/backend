package com.documentor.backend.presentation.share;

import com.documentor.backend.service.share.ShareLinkResult;
import java.time.LocalDateTime;

public record ShareLinkResponse(
        Long shareId,
        String shareToken,
        String shareUrl,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {

    public static ShareLinkResponse from(ShareLinkResult result) {
        return new ShareLinkResponse(
                result.shareId(),
                result.shareToken(),
                result.shareUrl(),
                result.expiresAt(),
                result.createdAt()
        );
    }
}
