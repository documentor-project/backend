package com.documentor.backend.infra.ai;

import com.documentor.backend.service.question.DocumentContextRetriever;
import com.documentor.backend.service.question.DocumentSourceExcerpt;
import com.documentor.backend.service.question.QuestionGenerationCommand;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Component
public class SpringAiDocumentContextRetriever implements DocumentContextRetriever {

    private final VectorStore vectorStore;

    public SpringAiDocumentContextRetriever(VectorStore documentVectorStore) {
        this.vectorStore = documentVectorStore;
    }

    @Override
    public List<DocumentSourceExcerpt> retrieve(Long documentId, QuestionGenerationCommand command, int topK) {
        String query = String.format("%s %s %s", command.field(), command.difficulty(), command.questionTypes());
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThresholdAll()
                .filterExpression("documentId == " + documentId)
                .build();

        return vectorStore.similaritySearch(searchRequest).stream()
                .map(document -> toExcerpt(documentId, document))
                .toList();
    }

    private DocumentSourceExcerpt toExcerpt(Long documentId, Document document) {
        Map<String, Object> metadata = document.getMetadata();
        int page = toInt(metadata.get("page"));
        int chunkIndex = toInt(metadata.get("chunkIndex"));
        return new DocumentSourceExcerpt(documentId, page, chunkIndex, document.getText());
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string && !string.isBlank()) {
            return Integer.parseInt(string);
        }
        return 0;
    }
}
