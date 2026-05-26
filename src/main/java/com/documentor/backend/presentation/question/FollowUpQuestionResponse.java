package com.documentor.backend.presentation.question;

import com.documentor.backend.service.question.FollowUpQuestionResult;

public record FollowUpQuestionResponse(
        Long followUpQuestionId,
        String content
) {

    public static FollowUpQuestionResponse from(FollowUpQuestionResult result) {
        return new FollowUpQuestionResponse(result.followUpQuestionId(), result.content());
    }
}
