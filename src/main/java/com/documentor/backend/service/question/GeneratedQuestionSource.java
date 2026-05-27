package com.documentor.backend.service.question;

public record GeneratedQuestionSource(
        Long documentId,
        int page,
        int chunkIndex,
        String snippet
) {
}
