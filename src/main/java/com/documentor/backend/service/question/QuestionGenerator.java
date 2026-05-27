package com.documentor.backend.service.question;

import java.util.List;

public interface QuestionGenerator {

    List<GeneratedQuestion> generate(QuestionGenerationCommand command, List<DocumentSourceExcerpt> sourceExcerpts);
}
