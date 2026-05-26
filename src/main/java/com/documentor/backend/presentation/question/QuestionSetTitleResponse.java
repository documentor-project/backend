package com.documentor.backend.presentation.question;

import com.documentor.backend.service.question.QuestionSetResult;
import java.time.LocalDateTime;

public record QuestionSetTitleResponse(
        Long questionSetId,
        String title,
        LocalDateTime updatedAt
) {

    public static QuestionSetTitleResponse from(QuestionSetResult result) {
        return new QuestionSetTitleResponse(result.questionSetId(), result.title(), result.updatedAt());
    }
}
