package com.documentor.backend.service.notification;

import java.time.LocalTime;

public record NotificationSettingCommand(
        boolean enabled,
        String email,
        LocalTime sendTime,
        int questionCount,
        Long questionSetId
) {
}
