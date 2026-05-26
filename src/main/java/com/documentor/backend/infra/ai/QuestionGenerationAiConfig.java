package com.documentor.backend.infra.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuestionGenerationAiConfig {

    @Bean
    public ChatClient questionGenerationChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You generate developer interview and practical work questions from technical documents.
                        Use only the provided document excerpts.
                        Do not create questions when the document excerpts do not provide enough evidence.
                        Return Korean text unless the source terms are code, APIs, or English technical names.
                        """)
                .build();
    }
}
