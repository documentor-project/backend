package com.documentor.backend.infra.ai;

import com.documentor.backend.service.question.DocumentSourceExcerpt;
import com.documentor.backend.service.question.QuestionGenerationCommand;
import com.documentor.backend.service.question.QuestionGenerator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class SpringAiQuestionGenerator implements QuestionGenerator {

    private final ChatClient chatClient;

    public SpringAiQuestionGenerator(ChatClient questionGenerationChatClient) {
        this.chatClient = questionGenerationChatClient;
    }

    @Override
    public String generate(QuestionGenerationCommand command, List<DocumentSourceExcerpt> sourceExcerpts) {
        String sourceText = sourceExcerpts.stream()
                .map(DocumentSourceExcerpt::toPromptText)
                .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
                .user("""
                        Generate questions using the conditions below.

                        Conditions:
                        - questionCount: %d
                        - difficulty: %s
                        - field: %s
                        - includeFollowUp: %s
                        - questionTypes: %s

                        Output rules:
                        - Return JSON only.
                        - Each question must include content, type, difficulty, source snippet, and followUps.
                        - If evidence is insufficient, skip that question.

                        Document excerpts:
                        %s
                        """.formatted(
                        command.questionCount(),
                        command.difficulty(),
                        command.field(),
                        command.includeFollowUp(),
                        command.questionTypes(),
                        sourceText
                ))
                .call()
                .content();
    }
}
