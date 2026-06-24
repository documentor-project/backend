package com.documentor.backend.presentation.notification;

import com.documentor.backend.service.notification.TestReviewEmailResult;

public record TestReviewEmailResponse(
        String email,
        int questionCount,
        String status
) {

    public static TestReviewEmailResponse from(TestReviewEmailResult result) {
        return new TestReviewEmailResponse(result.email(), result.questionCount(), "SENT");
    }
}
