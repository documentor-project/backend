package com.documentor.backend.presentation.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionSetTitleRequest(
        @NotBlank
        @Size(max = 100)
        String title
) {
}
