package com.documentor.backend.service.question;

import com.documentor.backend.domain.question.Question;
import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.domain.question.QuestionType;
import java.time.LocalDateTime;
import java.util.List;

public record QuestionResult(
        Long questionId,
        Long questionSetId,
        QuestionType type,
        QuestionDifficulty difficulty,
        String content,
        QuestionSourceResult source,
        List<FollowUpQuestionResult> followUps,
        boolean bookmarked,
        boolean answered,
        LocalDateTime answeredAt,
        LocalDateTime createdAt
) {

    public static QuestionResult from(Question question) {
        return new QuestionResult(
                question.getId(),
                question.getQuestionSet().getId(),
                question.getType(),
                question.getDifficulty(),
                question.getContent(),
                QuestionSourceResult.from(question.getSource(), question.getQuestionSet().getDocument().getTitle()),
                question.getFollowUps().stream().map(FollowUpQuestionResult::from).toList(),
                question.isBookmarked(),
                question.isAnswered(),
                question.getAnsweredAt(),
                question.getCreatedAt()
        );
    }
}
