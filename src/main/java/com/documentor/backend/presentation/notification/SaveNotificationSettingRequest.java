package com.documentor.backend.presentation.notification;

import com.documentor.backend.service.notification.NotificationSettingCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record SaveNotificationSettingRequest(
        boolean enabled,

        @Email
        String email,

        @NotNull
        LocalTime sendTime,

        @Min(1)
        @Max(10)
        int questionCount,

        Long questionSetId
) {

    public NotificationSettingCommand toCommand() {
        return new NotificationSettingCommand(enabled, email, sendTime, questionCount, questionSetId);
    }
}
