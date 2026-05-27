package com.documentor.backend.presentation.question;

import com.documentor.backend.service.question.QuestionGenerationResult;
import com.documentor.backend.service.question.QuestionGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QuestionGenerationController {

    private final QuestionGenerationService questionGenerationService;

    public QuestionGenerationController(QuestionGenerationService questionGenerationService) {
        this.questionGenerationService = questionGenerationService;
    }

    @PostMapping("/documents/{documentId}/question-generations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public QuestionGenerationResponse createQuestionGeneration(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long documentId,
            @Valid @RequestBody QuestionGenerationRequest request
    ) {
        QuestionGenerationResult result = questionGenerationService.create(authorizationHeader, documentId, request.toCommand());
        return QuestionGenerationResponse.from(result);
    }

    @GetMapping("/question-generations/{generationId}")
    public QuestionGenerationStatusResponse getQuestionGenerationStatus(@PathVariable Long generationId) {
        QuestionGenerationResult result = questionGenerationService.getStatus(generationId);
        return QuestionGenerationStatusResponse.from(result);
    }
}
