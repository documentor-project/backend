package com.documentor.backend.service.notification;

import com.documentor.backend.domain.notification.DeliveryStatus;
import com.documentor.backend.domain.notification.ReviewDelivery;
import java.time.LocalDateTime;

public record ReviewDeliveryResult(
        Long deliveryId,
        Long questionSetId,
        String questionSetTitle,
        String email,
        int questionCount,
        DeliveryStatus status,
        LocalDateTime sentAt
) {

    public static ReviewDeliveryResult from(ReviewDelivery delivery) {
        return new ReviewDeliveryResult(
                delivery.getId(),
                delivery.getQuestionSet().getId(),
                delivery.getQuestionSet().getTitle(),
                delivery.getEmail(),
                delivery.getQuestionCount(),
                delivery.getStatus(),
                delivery.getSentAt()
        );
    }
}
