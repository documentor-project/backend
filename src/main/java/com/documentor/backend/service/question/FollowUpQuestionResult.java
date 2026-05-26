package com.documentor.backend.service.question;

import com.documentor.backend.domain.question.FollowUpQuestion;

public record FollowUpQuestionResult(
        Long followUpQuestionId,
        String content
) {

    public static FollowUpQuestionResult from(FollowUpQuestion followUpQuestion) {
        return new FollowUpQuestionResult(followUpQuestion.getId(), followUpQuestion.getContent());
    }
}
