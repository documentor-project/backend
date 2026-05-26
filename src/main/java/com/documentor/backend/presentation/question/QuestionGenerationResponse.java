package com.documentor.backend.presentation.question;

import com.documentor.backend.domain.question.GenerationStatus;
import com.documentor.backend.service.question.QuestionGenerationResult;
import java.time.LocalDateTime;

public record QuestionGenerationResponse(
        Long generationId,
        Long documentId,
        GenerationStatus status,
        int requestedQuestionCount,
        LocalDateTime createdAt
) {

    public static QuestionGenerationResponse from(QuestionGenerationResult result) {
        return new QuestionGenerationResponse(
                result.generationId(),
                result.documentId(),
                result.status(),
                result.requestedQuestionCount(),
                result.createdAt()
        );
    }
}
