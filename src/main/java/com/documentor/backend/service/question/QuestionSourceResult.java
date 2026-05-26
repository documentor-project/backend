package com.documentor.backend.service.question;

import com.documentor.backend.domain.question.QuestionSource;

public record QuestionSourceResult(
        Long documentId,
        String documentTitle,
        Integer page,
        Integer chunkIndex,
        String snippet
) {

    public static QuestionSourceResult from(QuestionSource source, String documentTitle) {
        return new QuestionSourceResult(
                source.getDocumentId(),
                documentTitle,
                source.getPage(),
                source.getChunkIndex(),
                source.getSnippet()
        );
    }
}
