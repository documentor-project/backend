package com.documentor.backend.presentation.question;

import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.service.question.QuestionSetResult;
import java.time.LocalDateTime;

public record QuestionSetSummaryResponse(
        Long questionSetId,
        String title,
        Long documentId,
        String documentTitle,
        int questionCount,
        QuestionDifficulty difficulty,
        LocalDateTime createdAt
) {

    public static QuestionSetSummaryResponse from(QuestionSetResult result) {
        return new QuestionSetSummaryResponse(
                result.questionSetId(),
                result.title(),
                result.documentId(),
                result.documentTitle(),
                result.questionCount(),
                result.difficulty(),
                result.createdAt()
        );
    }
}
