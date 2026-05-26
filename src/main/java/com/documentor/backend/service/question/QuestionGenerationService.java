package com.documentor.backend.service.question;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class QuestionGenerationService {

    private final AtomicLong generationIdGenerator = new AtomicLong(1);
    private final Map<Long, QuestionGenerationResult> generations = new ConcurrentHashMap<>();

    public QuestionGenerationResult create(Long documentId, QuestionGenerationCommand command) {
        Long generationId = generationIdGenerator.getAndIncrement();
        QuestionGenerationResult result = QuestionGenerationResult.pending(
                generationId,
                documentId,
                command.questionCount()
        );
        generations.put(generationId, result);
        return result;
    }

    public QuestionGenerationResult getStatus(Long generationId) {
        QuestionGenerationResult result = generations.get(generationId);
        if (result == null) {
            throw new NoSuchElementException("Question generation not found. generationId=" + generationId);
        }
        return result;
    }
}
