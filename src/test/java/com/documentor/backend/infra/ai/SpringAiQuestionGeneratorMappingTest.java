package com.documentor.backend.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.documentor.backend.domain.question.QuestionDifficulty;
import com.documentor.backend.domain.question.QuestionField;
import com.documentor.backend.domain.question.QuestionType;
import com.documentor.backend.service.question.DocumentSourceExcerpt;
import com.documentor.backend.service.question.GeneratedQuestion;
import com.documentor.backend.service.question.QuestionGenerationCommand;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

class SpringAiQuestionGeneratorMappingTest {

    @Test
    void mapsStructuredAiResponseToGeneratedQuestions() {
        ChatModel chatModel = prompt -> null;
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        SpringAiQuestionGenerator generator = new SpringAiQuestionGenerator(chatClient);
        SpringAiQuestionGenerator.AiQuestion aiQuestion = new SpringAiQuestionGenerator.AiQuestion(
                "문서 임베딩 상태 전이의 목적을 설명해 주세요.",
                QuestionType.CONCEPT,
                QuestionDifficulty.BASIC,
                new SpringAiQuestionGenerator.AiQuestionSource(0, 3, "PARSING -> EMBEDDING -> READY"),
                List.of("실패 시 FAILED 상태를 언제 기록해야 하나요?")
        );

        GeneratedQuestion generatedQuestion = aiQuestion.toGeneratedQuestion(List.of(
                new DocumentSourceExcerpt(10L, 0, 3, "PARSING -> EMBEDDING -> READY")
        ));

        assertThat(generatedQuestion).isNotNull();
        assertThat(generatedQuestion.type()).isEqualTo(QuestionType.CONCEPT);
        assertThat(generatedQuestion.difficulty()).isEqualTo(QuestionDifficulty.BASIC);
        assertThat(generatedQuestion.source().documentId()).isEqualTo(10L);
        assertThat(generatedQuestion.source().chunkIndex()).isEqualTo(3);
        assertThat(generatedQuestion.followUps()).hasSize(1);
    }

    @Test
    void commandCanCarryAllQuestionTypesForPromptConditions() {
        QuestionGenerationCommand command = new QuestionGenerationCommand(
                4,
                QuestionDifficulty.ADVANCED,
                QuestionField.SPRING,
                true,
                List.of(QuestionType.CONCEPT, QuestionType.COMPARISON, QuestionType.PRACTICAL, QuestionType.TROUBLESHOOTING)
        );

        assertThat(command.questionTypes()).containsExactly(
                QuestionType.CONCEPT,
                QuestionType.COMPARISON,
                QuestionType.PRACTICAL,
                QuestionType.TROUBLESHOOTING
        );
    }
}
