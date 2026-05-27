package com.documentor.backend.infra.ai;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.document.DocumentFileType;
import com.documentor.backend.service.document.DocumentVectorIndexer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class SpringAiDocumentVectorIndexer implements DocumentVectorIndexer {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public SpringAiDocumentVectorIndexer(VectorStore documentVectorStore) {
        this.vectorStore = documentVectorStore;
        this.textSplitter = new TokenTextSplitter();
    }

    @Override
    public int index(Long documentId, DocumentFileType fileType, Path filePath) {
        List<Document> rawDocuments = createReader(fileType, filePath).get();
        List<Document> splitDocuments = textSplitter.apply(rawDocuments);
        List<Document> indexedDocuments = new ArrayList<>();

        for (int i = 0; i < splitDocuments.size(); i++) {
            Document chunk = splitDocuments.get(i);
            String text = chunk.getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
            metadata.put("documentId", documentId);
            metadata.put("chunkIndex", i);
            metadata.put("fileType", fileType.name());
            metadata.putIfAbsent("page", 0);
            indexedDocuments.add(new Document(text, metadata));
        }

        if (indexedDocuments.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_RESOURCE_STATE, "문서에서 임베딩할 수 있는 텍스트를 찾지 못했습니다.");
        }

        vectorStore.add(indexedDocuments);
        return indexedDocuments.size();
    }

    private DocumentReader createReader(DocumentFileType fileType, Path filePath) {
        FileSystemResource resource = new FileSystemResource(filePath);
        return switch (fileType) {
            case PDF -> new TikaDocumentReader(resource);
            case MD, TXT -> new TextReader(resource);
        };
    }
}
