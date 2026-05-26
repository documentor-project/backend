package com.documentor.backend.presentation.question;

import com.documentor.backend.domain.question.GenerationStatus;
import com.documentor.backend.service.question.QuestionGenerationResult;
import java.time.LocalDateTime;

public record QuestionGenerationStatusResponse(
        Long generationId,
        Long documentId,
        GenerationStatus status,
        int progress,
        int createdQuestionCount,
        int skippedQuestionCount,
        String skipReason,
        Long questionSetId,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    public static QuestionGenerationStatusResponse from(QuestionGenerationResult result) {
        return new QuestionGenerationStatusResponse(
                result.generationId(),
                result.documentId(),
                result.status(),
                result.progress(),
                result.createdQuestionCount(),
                result.skippedQuestionCount(),
                result.skipReason(),
                result.questionSetId(),
                result.createdAt(),
                result.completedAt()
        );
    }
}
