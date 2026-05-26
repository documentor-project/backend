package com.documentor.backend.presentation.notification;

import com.documentor.backend.domain.notification.DeliveryStatus;
import com.documentor.backend.service.notification.ReviewDeliveryResult;
import java.time.LocalDateTime;

public record ReviewDeliveryResponse(
        Long deliveryId,
        Long questionSetId,
        String questionSetTitle,
        String email,
        int questionCount,
        DeliveryStatus status,
        LocalDateTime sentAt
) {

    public static ReviewDeliveryResponse from(ReviewDeliveryResult result) {
        return new ReviewDeliveryResponse(
                result.deliveryId(),
                result.questionSetId(),
                result.questionSetTitle(),
                result.email(),
                result.questionCount(),
                result.status(),
                result.sentAt()
        );
    }
}
