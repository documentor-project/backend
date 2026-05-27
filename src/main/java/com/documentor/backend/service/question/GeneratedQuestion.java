package com.documentor.backend.service.question;

import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.domain.question.QuestionType;
import java.util.List;

public record GeneratedQuestion(
        String content,
        QuestionType type,
        QuestionDifficulty difficulty,
        GeneratedQuestionSource source,
        List<String> followUps
) {
}
