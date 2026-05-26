package com.documentor.backend.service.question;

public record DocumentSourceExcerpt(
        Long documentId,
        Integer page,
        Integer chunkIndex,
        String snippet
) {

    public String toPromptText() {
        return """
                documentId: %d
                page: %s
                chunkIndex: %s
                snippet:
                %s
                """.formatted(documentId, page, chunkIndex, snippet);
    }
}
