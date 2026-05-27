package com.documentor.backend.service.document;

import com.documentor.backend.domain.document.DocumentFileType;
import java.nio.file.Path;

public interface DocumentVectorIndexer {

    int index(Long documentId, DocumentFileType fileType, Path filePath);
}
