package com.documentor.backend.presentation.question;

import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.domain.question.QuestionType;
import com.documentor.backend.service.question.QuestionResult;
import java.time.LocalDateTime;
import java.util.List;

public record QuestionResponse(
        Long questionId,
        Long questionSetId,
        QuestionType type,
        QuestionDifficulty difficulty,
        String content,
        QuestionSourceResponse source,
        List<FollowUpQuestionResponse> followUps,
        boolean bookmarked,
        boolean answered,
        LocalDateTime answeredAt,
        LocalDateTime createdAt
) {

    public static QuestionResponse from(QuestionResult result) {
        return new QuestionResponse(
                result.questionId(),
                result.questionSetId(),
                result.type(),
                result.difficulty(),
                result.content(),
                QuestionSourceResponse.from(result.source()),
                result.followUps().stream().map(FollowUpQuestionResponse::from).toList(),
                result.bookmarked(),
                result.answered(),
                result.answeredAt(),
                result.createdAt()
        );
    }
}
