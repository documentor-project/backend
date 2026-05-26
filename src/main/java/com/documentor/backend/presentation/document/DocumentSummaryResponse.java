package com.documentor.backend.presentation.document;

import com.documentor.backend.domain.document.DocumentFileType;
import com.documentor.backend.domain.document.DocumentStatus;
import com.documentor.backend.service.document.DocumentResult;
import java.time.LocalDateTime;

public record DocumentSummaryResponse(
        Long documentId,
        String title,
        DocumentFileType fileType,
        DocumentStatus status,
        LocalDateTime createdAt
) {

    public static DocumentSummaryResponse from(DocumentResult result) {
        return new DocumentSummaryResponse(
                result.documentId(),
                result.title(),
                result.fileType(),
                result.status(),
                result.createdAt()
        );
    }
}
