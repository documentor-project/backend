package com.documentor.backend.service.question;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.document.DocumentStatus;
import com.documentor.backend.domain.document.TechnicalDocument;
import com.documentor.backend.domain.question.Question;
import com.documentor.backend.domain.question.QuestionSet;
import com.documentor.backend.domain.question.QuestionSource;
import com.documentor.backend.infra.document.TechnicalDocumentRepository;
import com.documentor.backend.infra.question.QuestionSetRepository;
import com.documentor.backend.infra.security.AuthenticatedUserResolver;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionGenerationService {

    private static final int MIN_CONTEXT_TOP_K = 5;
    private static final int CONTEXT_MULTIPLIER = 2;

    private final AtomicLong generationIdGenerator = new AtomicLong(1);
    private final Map<Long, QuestionGenerationResult> generations = new ConcurrentHashMap<>();
    private final TechnicalDocumentRepository documentRepository;
    private final QuestionSetRepository questionSetRepository;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final DocumentContextRetriever documentContextRetriever;
    private final QuestionGenerator questionGenerator;

    public QuestionGenerationService(
            TechnicalDocumentRepository documentRepository,
            QuestionSetRepository questionSetRepository,
            AuthenticatedUserResolver authenticatedUserResolver,
            DocumentContextRetriever documentContextRetriever,
            QuestionGenerator questionGenerator
    ) {
        this.documentRepository = documentRepository;
        this.questionSetRepository = questionSetRepository;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.documentContextRetriever = documentContextRetriever;
        this.questionGenerator = questionGenerator;
    }

    @Transactional
    public QuestionGenerationResult create(String authorizationHeader, Long documentId, QuestionGenerationCommand command) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        TechnicalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없습니다."));
        if (!document.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "문서에 접근할 수 없습니다.");
        }
        if (document.getStatus() != DocumentStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_RESOURCE_STATE, "문서 임베딩이 완료된 후 질문을 생성할 수 있습니다.");
        }

        Long generationId = generationIdGenerator.getAndIncrement();
        QuestionGenerationResult pending = QuestionGenerationResult.pending(generationId, documentId, command.questionCount());
        generations.put(generationId, pending.processing());

        try {
            List<DocumentSourceExcerpt> excerpts = documentContextRetriever.retrieve(
                    documentId,
                    command,
                    Math.max(MIN_CONTEXT_TOP_K, command.questionCount() * CONTEXT_MULTIPLIER)
            );
            if (excerpts.isEmpty()) {
                QuestionGenerationResult completed = pending.completed(0, command.questionCount(), "질문 생성에 사용할 관련 문서 청크를 찾지 못했습니다.", null);
                generations.put(generationId, completed);
                return completed;
            }

            List<GeneratedQuestion> generatedQuestions = questionGenerator.generate(command, excerpts);
            if (generatedQuestions.isEmpty()) {
                QuestionGenerationResult completed = pending.completed(0, command.questionCount(), "AI가 근거가 충분한 질문을 생성하지 못했습니다.", null);
                generations.put(generationId, completed);
                return completed;
            }

            QuestionSet questionSet = QuestionSet.create(document.getOwner(), document, createTitle(document), command.difficulty());
            generatedQuestions.stream()
                    .limit(command.questionCount())
                    .forEach(generatedQuestion -> addQuestion(questionSet, generatedQuestion));
            QuestionSet savedQuestionSet = questionSetRepository.save(questionSet);

            int createdCount = savedQuestionSet.getQuestions().size();
            int skippedCount = Math.max(0, command.questionCount() - createdCount);
            QuestionGenerationResult completed = pending.completed(createdCount, skippedCount, skippedCount > 0 ? "근거가 부족한 일부 질문을 제외했습니다." : null, savedQuestionSet.getId());
            generations.put(generationId, completed);
            return completed;
        } catch (BusinessException e) {
            QuestionGenerationResult failed = pending.failed(e.getMessage());
            generations.put(generationId, failed);
            throw e;
        } catch (RuntimeException e) {
            QuestionGenerationResult failed = pending.failed("질문 생성 중 오류가 발생했습니다.");
            generations.put(generationId, failed);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "질문 생성 중 오류가 발생했습니다.");
        }
    }

    public QuestionGenerationResult getStatus(Long generationId) {
        QuestionGenerationResult result = generations.get(generationId);
        if (result == null) {
            throw new NoSuchElementException("Question generation not found. generationId=" + generationId);
        }
        return result;
    }

    private void addQuestion(QuestionSet questionSet, GeneratedQuestion generatedQuestion) {
        GeneratedQuestionSource source = generatedQuestion.source();
        QuestionSource questionSource = QuestionSource.create(
                source.documentId(),
                source.page(),
                source.chunkIndex(),
                abbreviate(source.snippet(), 2000)
        );
        Question question = Question.create(
                questionSet,
                generatedQuestion.type(),
                generatedQuestion.difficulty(),
                generatedQuestion.content(),
                questionSource
        );
        generatedQuestion.followUps().forEach(question::addFollowUp);
        questionSet.addQuestion(question);
    }

    private String createTitle(TechnicalDocument document) {
        return "AI 질문 세트 - " + document.getTitle();
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
