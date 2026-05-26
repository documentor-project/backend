package com.documentor.backend.domain.question;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class QuestionSource {

    @Column(name = "source_document_id")
    private Long documentId;

    @Column(name = "source_page")
    private Integer page;

    @Column(name = "source_chunk_index")
    private Integer chunkIndex;

    @Column(name = "source_snippet", length = 2000)
    private String snippet;

    protected QuestionSource() {
    }

    private QuestionSource(Long documentId, Integer page, Integer chunkIndex, String snippet) {
        this.documentId = documentId;
        this.page = page;
        this.chunkIndex = chunkIndex;
        this.snippet = snippet;
    }

    public static QuestionSource create(Long documentId, Integer page, Integer chunkIndex, String snippet) {
        return new QuestionSource(documentId, page, chunkIndex, snippet);
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public String getSnippet() {
        return snippet;
    }
}
