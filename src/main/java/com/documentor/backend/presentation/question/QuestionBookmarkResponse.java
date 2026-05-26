package com.documentor.backend.presentation.question;

public record QuestionBookmarkResponse(
        Long questionId,
        boolean bookmarked
) {
}
