package com.documentor.backend.presentation.question;

import com.documentor.backend.service.question.QuestionSourceResult;

public record QuestionSourceResponse(
        Long documentId,
        String documentTitle,
        Integer page,
        Integer chunkIndex,
        String snippet
) {

    public static QuestionSourceResponse from(QuestionSourceResult result) {
        return new QuestionSourceResponse(
                result.documentId(),
                result.documentTitle(),
                result.page(),
                result.chunkIndex(),
                result.snippet()
        );
    }
}
