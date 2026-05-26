package com.documentor.backend.presentation.notification;

import com.documentor.backend.service.notification.NotificationSettingResult;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record NotificationSettingResponse(
        boolean enabled,
        String email,
        LocalTime sendTime,
        int questionCount,
        Long questionSetId,
        String questionSetTitle,
        LocalDateTime updatedAt
) {

    public static NotificationSettingResponse from(NotificationSettingResult result) {
        return new NotificationSettingResponse(
                result.enabled(),
                result.email(),
                result.sendTime(),
                result.questionCount(),
                result.questionSetId(),
                result.questionSetTitle(),
                result.updatedAt()
        );
    }
}
