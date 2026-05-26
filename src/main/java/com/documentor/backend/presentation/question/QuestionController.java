package com.documentor.backend.presentation.question;

import com.documentor.backend.presentation.document.PageResponse;
import com.documentor.backend.service.question.QuestionResult;
import com.documentor.backend.service.question.QuestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/question-sets")
    public PageResponse<QuestionSetSummaryResponse> getQuestionSets(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) Long documentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<QuestionSetSummaryResponse> result = questionService.getQuestionSets(
                authorizationHeader,
                documentId,
                PageRequest.of(page, size)
        ).map(QuestionSetSummaryResponse::from);
        return PageResponse.from(result);
    }

    @GetMapping("/question-sets/{questionSetId}")
    public QuestionSetDetailResponse getQuestionSet(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long questionSetId
    ) {
        return QuestionSetDetailResponse.from(questionService.getQuestionSet(authorizationHeader, questionSetId));
    }

    @PatchMapping("/question-sets/{questionSetId}")
    public QuestionSetTitleResponse updateQuestionSetTitle(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long questionSetId,
            @Valid @RequestBody QuestionSetTitleRequest request
    ) {
        return QuestionSetTitleResponse.from(questionService.updateQuestionSetTitle(authorizationHeader, questionSetId, request.title()));
    }

    @DeleteMapping("/question-sets/{questionSetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestionSet(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long questionSetId
    ) {
        questionService.deleteQuestionSet(authorizationHeader, questionSetId);
    }

    @GetMapping("/questions/{questionId}")
    public QuestionResponse getQuestion(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long questionId
    ) {
        return QuestionResponse.from(questionService.getQuestion(authorizationHeader, questionId));
    }

    @PatchMapping("/questions/{questionId}/bookmark")
    public QuestionBookmarkResponse updateBookmark(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long questionId,
            @RequestBody BookmarkRequest request
    ) {
        QuestionResult result = questionService.updateBookmark(authorizationHeader, questionId, request.bookmarked());
        return new QuestionBookmarkResponse(result.questionId(), result.bookmarked());
    }

    @PatchMapping("/questions/{questionId}/answer-status")
    public QuestionAnswerStatusResponse updateAnswerStatus(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long questionId,
            @RequestBody AnswerStatusRequest request
    ) {
        QuestionResult result = questionService.updateAnswerStatus(authorizationHeader, questionId, request.answered());
        return new QuestionAnswerStatusResponse(result.questionId(), result.answered(), result.answeredAt());
    }
}
