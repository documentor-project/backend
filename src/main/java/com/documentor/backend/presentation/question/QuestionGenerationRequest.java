package com.documentor.backend.presentation.question;

import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.domain.question.QuestionField;
import com.documentor.backend.domain.question.QuestionType;
import com.documentor.backend.service.question.QuestionGenerationCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuestionGenerationRequest(
        @Min(1)
        @Max(30)
        int questionCount,

        @NotNull
        QuestionDifficulty difficulty,

        @NotNull
        QuestionField field,

        boolean includeFollowUp,

        @NotNull
        @Size(min = 1)
        List<QuestionType> questionTypes
) {

    public QuestionGenerationCommand toCommand() {
        return new QuestionGenerationCommand(
                questionCount,
                difficulty,
                field,
                includeFollowUp,
                questionTypes
        );
    }
}
