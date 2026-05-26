package com.documentor.backend.service.question;

import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.domain.question.QuestionSet;
import java.time.LocalDateTime;
import java.util.List;

public record QuestionSetResult(
        Long questionSetId,
        String title,
        Long documentId,
        String documentTitle,
        int questionCount,
        QuestionDifficulty difficulty,
        List<QuestionResult> questions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static QuestionSetResult from(QuestionSet questionSet) {
        return new QuestionSetResult(
                questionSet.getId(),
                questionSet.getTitle(),
                questionSet.getDocument().getId(),
                questionSet.getDocument().getTitle(),
                questionSet.getQuestions().size(),
                questionSet.getDifficulty(),
                questionSet.getQuestions().stream().map(QuestionResult::from).toList(),
                questionSet.getCreatedAt(),
                questionSet.getUpdatedAt()
        );
    }
}
