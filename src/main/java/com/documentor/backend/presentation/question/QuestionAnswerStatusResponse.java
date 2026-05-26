package com.documentor.backend.presentation.question;

import java.time.LocalDateTime;

public record QuestionAnswerStatusResponse(
        Long questionId,
        boolean answered,
        LocalDateTime answeredAt
) {
}
