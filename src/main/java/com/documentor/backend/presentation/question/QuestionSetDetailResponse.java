package com.documentor.backend.presentation.question;

import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.service.question.QuestionSetResult;
import java.time.LocalDateTime;
import java.util.List;

public record QuestionSetDetailResponse(
        Long questionSetId,
        String title,
        Long documentId,
        String documentTitle,
        QuestionDifficulty difficulty,
        List<QuestionResponse> questions,
        LocalDateTime createdAt
) {

    public static QuestionSetDetailResponse from(QuestionSetResult result) {
        return new QuestionSetDetailResponse(
                result.questionSetId(),
                result.title(),
                result.documentId(),
                result.documentTitle(),
                result.difficulty(),
                result.questions().stream().map(QuestionResponse::from).toList(),
                result.createdAt()
        );
    }
}
