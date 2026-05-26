package com.documentor.backend.presentation.document;

import com.documentor.backend.domain.document.DocumentFileType;
import com.documentor.backend.domain.document.DocumentStatus;
import com.documentor.backend.service.document.DocumentResult;
import java.time.LocalDateTime;

public record DocumentResponse(
        Long documentId,
        String title,
        String fileName,
        DocumentFileType fileType,
        DocumentStatus status,
        int chunkCount,
        LocalDateTime createdAt
) {

    public static DocumentResponse from(DocumentResult result) {
        return new DocumentResponse(
                result.documentId(),
                result.title(),
                result.fileName(),
                result.fileType(),
                result.status(),
                result.chunkCount(),
                result.createdAt()
        );
    }
}
