package com.documentor.backend.service.share;

import com.documentor.backend.domain.share.ShareLink;
import java.time.LocalDateTime;

public record ShareLinkResult(
        Long shareId,
        String shareToken,
        String shareUrl,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {

    public static ShareLinkResult from(ShareLink shareLink, String shareBaseUrl) {
        return new ShareLinkResult(
                shareLink.getId(),
                shareLink.getToken(),
                shareBaseUrl + "/" + shareLink.getToken(),
                shareLink.getExpiresAt(),
                shareLink.getCreatedAt()
        );
    }
}
