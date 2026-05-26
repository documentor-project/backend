package com.documentor.backend.service.question;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.question.Question;
import com.documentor.backend.domain.question.QuestionSet;
import com.documentor.backend.infra.question.QuestionRepository;
import com.documentor.backend.infra.question.QuestionSetRepository;
import com.documentor.backend.infra.security.AuthenticatedUserResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private final QuestionSetRepository questionSetRepository;
    private final QuestionRepository questionRepository;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public QuestionService(
            QuestionSetRepository questionSetRepository,
            QuestionRepository questionRepository,
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        this.questionSetRepository = questionSetRepository;
        this.questionRepository = questionRepository;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    public Page<QuestionSetResult> getQuestionSets(String authorizationHeader, Long documentId, Pageable pageable) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        Page<QuestionSet> questionSets = documentId == null
                ? questionSetRepository.findAllByOwnerId(userId, pageable)
                : questionSetRepository.findAllByOwnerIdAndDocumentId(userId, documentId, pageable);
        return questionSets.map(QuestionSetResult::from);
    }

    public QuestionSetResult getQuestionSet(String authorizationHeader, Long questionSetId) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        return QuestionSetResult.from(getOwnedQuestionSet(userId, questionSetId));
    }

    public QuestionSetResult updateQuestionSetTitle(String authorizationHeader, Long questionSetId, String title) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        QuestionSet questionSet = getOwnedQuestionSet(userId, questionSetId);
        questionSet.updateTitle(title);
        return QuestionSetResult.from(questionSetRepository.save(questionSet));
    }

    public void deleteQuestionSet(String authorizationHeader, Long questionSetId) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        questionSetRepository.delete(getOwnedQuestionSet(userId, questionSetId));
    }

    public QuestionResult getQuestion(String authorizationHeader, Long questionId) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        return QuestionResult.from(getOwnedQuestion(userId, questionId));
    }

    public QuestionResult updateBookmark(String authorizationHeader, Long questionId, boolean bookmarked) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        Question question = getOwnedQuestion(userId, questionId);
        question.updateBookmark(bookmarked);
        return QuestionResult.from(questionRepository.save(question));
    }

    public QuestionResult updateAnswerStatus(String authorizationHeader, Long questionId, boolean answered) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        Question question = getOwnedQuestion(userId, questionId);
        question.updateAnswered(answered);
        return QuestionResult.from(questionRepository.save(question));
    }

    private QuestionSet getOwnedQuestionSet(Long userId, Long questionSetId) {
        QuestionSet questionSet = questionSetRepository.findById(questionSetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "질문 리스트를 찾을 수 없습니다."));
        if (!questionSet.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "질문 리스트에 접근할 수 없습니다.");
        }
        return questionSet;
    }

    private Question getOwnedQuestion(Long userId, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "질문을 찾을 수 없습니다."));
        if (!question.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "질문에 접근할 수 없습니다.");
        }
        return question;
    }
}
