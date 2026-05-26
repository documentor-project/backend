package com.documentor.backend.service.question;

import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.domain.question.QuestionField;
import com.documentor.backend.domain.question.QuestionType;
import java.util.List;

public record QuestionGenerationCommand(
        int questionCount,
        QuestionDifficulty difficulty,
        QuestionField field,
        boolean includeFollowUp,
        List<QuestionType> questionTypes
) {
}
