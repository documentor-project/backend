package com.documentor.backend.service.notification;

public record TestReviewEmailResult(
        String email,
        int questionCount
) {
}
