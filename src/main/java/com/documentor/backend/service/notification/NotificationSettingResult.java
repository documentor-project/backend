package com.documentor.backend.service.notification;

import com.documentor.backend.domain.notification.NotificationSetting;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record NotificationSettingResult(
        boolean enabled,
        String email,
        LocalTime sendTime,
        int questionCount,
        Long questionSetId,
        String questionSetTitle,
        LocalDateTime updatedAt
) {

    public static NotificationSettingResult from(NotificationSetting setting) {
        return new NotificationSettingResult(
                setting.isEnabled(),
                setting.getEmail(),
                setting.getSendTime(),
                setting.getQuestionCount(),
                setting.getQuestionSet() == null ? null : setting.getQuestionSet().getId(),
                setting.getQuestionSet() == null ? null : setting.getQuestionSet().getTitle(),
                setting.getUpdatedAt()
        );
    }
}
