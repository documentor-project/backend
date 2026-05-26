package com.documentor.backend.service.document;

import com.documentor.backend.domain.document.DocumentFileType;
import com.documentor.backend.domain.document.DocumentStatus;
import com.documentor.backend.domain.document.TechnicalDocument;
import java.time.LocalDateTime;

public record DocumentResult(
        Long documentId,
        String title,
        String fileName,
        DocumentFileType fileType,
        DocumentStatus status,
        int chunkCount,
        LocalDateTime createdAt
) {

    public static DocumentResult from(TechnicalDocument document) {
        return new DocumentResult(
                document.getId(),
                document.getTitle(),
                document.getFileName(),
                document.getFileType(),
                document.getStatus(),
                document.getChunkCount(),
                document.getCreatedAt()
        );
    }
}
