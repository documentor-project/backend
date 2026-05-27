package com.documentor.backend.infra.ai;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.domain.question.QuestionType;
import com.documentor.backend.service.question.DocumentSourceExcerpt;
import com.documentor.backend.service.question.GeneratedQuestion;
import com.documentor.backend.service.question.GeneratedQuestionSource;
import com.documentor.backend.service.question.QuestionGenerationCommand;
import com.documentor.backend.service.question.QuestionGenerator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

@Component
public class SpringAiQuestionGenerator implements QuestionGenerator {

    private final ChatClient chatClient;
    private final BeanOutputConverter<QuestionGenerationAiResponse> outputConverter;

    public SpringAiQuestionGenerator(ChatClient questionGenerationChatClient) {
        this.chatClient = questionGenerationChatClient;
        this.outputConverter = new BeanOutputConverter<>(QuestionGenerationAiResponse.class);
    }

    @Override
    public List<GeneratedQuestion> generate(QuestionGenerationCommand command, List<DocumentSourceExcerpt> sourceExcerpts) {
        String sourceText = sourceExcerpts.stream()
                .map(DocumentSourceExcerpt::toPromptText)
                .collect(Collectors.joining("\n\n"));

        String content = chatClient.prompt()
                .user("""
                        Generate developer interview questions using the conditions and excerpts below.

                        Conditions:
                        - questionCount: %d
                        - difficulty: %s
                        - field: %s
                        - includeFollowUp: %s
                        - allowed questionTypes: %s

                        Generation requirements:
                        - Produce at most questionCount questions.
                        - Use only allowed questionTypes.
                        - Match every question difficulty to the requested difficulty unless the evidence strongly requires a lower difficulty.
                        - Each question must cite exactly one source object derived from the provided excerpt metadata.
                        - source.snippet must be a short direct evidence excerpt, not a summary.
                        - followUps must be empty when includeFollowUp is false.
                        - When includeFollowUp is true, provide one or two deeper follow-up questions.
                        - Skip questions whose answer cannot be grounded in the excerpts.

                        Structured output format:
                        %s

                        Document excerpts:
                        %s
                        """.formatted(
                        command.questionCount(),
                        command.difficulty(),
                        command.field(),
                        command.includeFollowUp(),
                        command.questionTypes(),
                        outputConverter.getFormat(),
                        sourceText
                ))
                .call()
                .content();

        try {
            QuestionGenerationAiResponse response = outputConverter.convert(content);
            if (response == null || response.questions() == null) {
                return List.of();
            }
            return response.questions().stream()
                    .map(question -> question.toGeneratedQuestion(sourceExcerpts))
                    .filter(Objects::nonNull)
                    .toList();
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 질문 생성 응답을 구조화된 결과로 변환하지 못했습니다.");
        }
    }

    public record QuestionGenerationAiResponse(List<AiQuestion> questions) {
    }

    public record AiQuestion(
            String content,
            QuestionType type,
            QuestionDifficulty difficulty,
            AiQuestionSource source,
            List<String> followUps
    ) {
        GeneratedQuestion toGeneratedQuestion(List<DocumentSourceExcerpt> sourceExcerpts) {
            if (content == null || content.isBlank() || type == null || difficulty == null || source == null) {
                return null;
            }
            DocumentSourceExcerpt matchedSource = sourceExcerpts.stream()
                    .filter(excerpt -> excerpt.chunkIndex() == source.chunkIndex())
                    .findFirst()
                    .orElse(sourceExcerpts.isEmpty() ? null : sourceExcerpts.getFirst());
            if (matchedSource == null) {
                return null;
            }
            String snippet = source.snippet() == null || source.snippet().isBlank()
                    ? matchedSource.snippet()
                    : source.snippet();
            GeneratedQuestionSource generatedSource = new GeneratedQuestionSource(
                    matchedSource.documentId(),
                    matchedSource.page(),
                    matchedSource.chunkIndex(),
                    snippet
            );
            return new GeneratedQuestion(content, type, difficulty, generatedSource, followUps == null ? List.of() : followUps);
        }
    }

    public record AiQuestionSource(
            int page,
            int chunkIndex,
            String snippet
    ) {
    }
}
