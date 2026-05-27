package com.documentor.backend.service.document;

import com.documentor.backend.domain.document.TechnicalDocument;
import com.documentor.backend.infra.document.TechnicalDocumentRepository;
import java.nio.file.Path;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentEmbeddingService {

    private final TechnicalDocumentRepository documentRepository;
    private final DocumentVectorIndexer documentVectorIndexer;

    public DocumentEmbeddingService(
            TechnicalDocumentRepository documentRepository,
            DocumentVectorIndexer documentVectorIndexer
    ) {
        this.documentRepository = documentRepository;
        this.documentVectorIndexer = documentVectorIndexer;
    }

    @Async
    @Transactional
    public void process(Long documentId, Path filePath) {
        TechnicalDocument document = documentRepository.findById(documentId).orElseThrow();
        try {
            document.markParsing();
            document.markEmbedding();
            int chunkCount = documentVectorIndexer.index(document.getId(), document.getFileType(), filePath);
            document.markReady(chunkCount);
        } catch (RuntimeException e) {
            document.markFailed();
        }
    }
}
