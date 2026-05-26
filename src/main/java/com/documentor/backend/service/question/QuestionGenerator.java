package com.documentor.backend.service.question;

import java.util.List;

public interface QuestionGenerator {

    String generate(QuestionGenerationCommand command, List<DocumentSourceExcerpt> sourceExcerpts);
}
