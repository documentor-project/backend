package com.documentor.backend.presentation.share;

import com.documentor.backend.presentation.question.QuestionSetDetailResponse;
import com.documentor.backend.service.share.ShareLinkResult;
import com.documentor.backend.service.share.ShareService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/question-sets/{questionSetId}/share-links")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareLinkResponse createShareLink(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long questionSetId,
            @RequestBody(required = false) CreateShareLinkRequest request
    ) {
        ShareLinkResult result = shareService.createShareLink(
                authorizationHeader,
                questionSetId,
                request == null ? null : request.expiresAt()
        );
        return ShareLinkResponse.from(result);
    }

    @GetMapping("/shared-question-sets/{shareToken}")
    public QuestionSetDetailResponse getSharedQuestionSet(@PathVariable String shareToken) {
        return QuestionSetDetailResponse.from(shareService.getSharedQuestionSet(shareToken));
    }

    @DeleteMapping("/share-links/{shareId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShareLink(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long shareId
    ) {
        shareService.deleteShareLink(authorizationHeader, shareId);
    }
}
