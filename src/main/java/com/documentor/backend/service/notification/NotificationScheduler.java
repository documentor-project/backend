package com.documentor.backend.service.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private final NotificationService notificationService;

    public NotificationScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void sendDueReviewQuestions() {
        notificationService.sendDueReviewQuestions();
    }
}
