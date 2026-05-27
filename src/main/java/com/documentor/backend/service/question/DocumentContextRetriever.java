package com.documentor.backend.service.question;

import java.util.List;

public interface DocumentContextRetriever {

    List<DocumentSourceExcerpt> retrieve(Long documentId, QuestionGenerationCommand command, int topK);
}
