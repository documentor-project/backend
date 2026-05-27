package com.documentor.backend.service.question;

import com.documentor.backend.domain.question.GenerationStatus;
import java.time.LocalDateTime;

public record QuestionGenerationResult(
        Long generationId,
        Long documentId,
        GenerationStatus status,
        int progress,
        int requestedQuestionCount,
        int createdQuestionCount,
        int skippedQuestionCount,
        String skipReason,
        Long questionSetId,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    public static QuestionGenerationResult pending(Long generationId, Long documentId, int requestedQuestionCount) {
        return new QuestionGenerationResult(
                generationId,
                documentId,
                GenerationStatus.PENDING,
                0,
                requestedQuestionCount,
                0,
                0,
                null,
                null,
                LocalDateTime.now(),
                null
        );
    }

    public QuestionGenerationResult processing() {
        return new QuestionGenerationResult(
                generationId,
                documentId,
                GenerationStatus.PROCESSING,
                50,
                requestedQuestionCount,
                createdQuestionCount,
                skippedQuestionCount,
                skipReason,
                questionSetId,
                createdAt,
                null
        );
    }

    public QuestionGenerationResult completed(int createdQuestionCount, int skippedQuestionCount, String skipReason, Long questionSetId) {
        return new QuestionGenerationResult(
                generationId,
                documentId,
                GenerationStatus.COMPLETED,
                100,
                requestedQuestionCount,
                createdQuestionCount,
                skippedQuestionCount,
                skipReason,
                questionSetId,
                createdAt,
                LocalDateTime.now()
        );
    }

    public QuestionGenerationResult failed(String skipReason) {
        return new QuestionGenerationResult(
                generationId,
                documentId,
                GenerationStatus.FAILED,
                100,
                requestedQuestionCount,
                createdQuestionCount,
                requestedQuestionCount,
                skipReason,
                questionSetId,
                createdAt,
                LocalDateTime.now()
        );
    }
}
