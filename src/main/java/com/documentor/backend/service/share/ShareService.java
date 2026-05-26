package com.documentor.backend.service.share;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.question.QuestionSet;
import com.documentor.backend.domain.share.ShareLink;
import com.documentor.backend.infra.question.QuestionSetRepository;
import com.documentor.backend.infra.security.AuthenticatedUserResolver;
import com.documentor.backend.infra.share.ShareLinkRepository;
import com.documentor.backend.service.question.QuestionSetResult;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final QuestionSetRepository questionSetRepository;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String shareBaseUrl;

    public ShareService(
            ShareLinkRepository shareLinkRepository,
            QuestionSetRepository questionSetRepository,
            AuthenticatedUserResolver authenticatedUserResolver,
            @Value("${app.share.base-url:http://localhost:8080/share}") String shareBaseUrl
    ) {
        this.shareLinkRepository = shareLinkRepository;
        this.questionSetRepository = questionSetRepository;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.shareBaseUrl = shareBaseUrl;
    }

    public ShareLinkResult createShareLink(String authorizationHeader, Long questionSetId, LocalDateTime expiresAt) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        QuestionSet questionSet = getOwnedQuestionSet(userId, questionSetId);
        ShareLink shareLink = ShareLink.create(questionSet, createToken(), expiresAt);
        return ShareLinkResult.from(shareLinkRepository.save(shareLink), shareBaseUrl);
    }

    public QuestionSetResult getSharedQuestionSet(String shareToken) {
        ShareLink shareLink = shareLinkRepository.findByToken(shareToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "공유 링크를 찾을 수 없습니다."));
        if (shareLink.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_RESOURCE_STATE, "만료된 공유 링크입니다.");
        }
        return QuestionSetResult.from(shareLink.getQuestionSet());
    }

    public void deleteShareLink(String authorizationHeader, Long shareId) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        ShareLink shareLink = shareLinkRepository.findById(shareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "공유 링크를 찾을 수 없습니다."));
        if (!shareLink.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "공유 링크에 접근할 수 없습니다.");
        }
        shareLinkRepository.delete(shareLink);
    }

    private QuestionSet getOwnedQuestionSet(Long userId, Long questionSetId) {
        QuestionSet questionSet = questionSetRepository.findById(questionSetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "질문 리스트를 찾을 수 없습니다."));
        if (!questionSet.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "질문 리스트에 접근할 수 없습니다.");
        }
        return questionSet;
    }

    private String createToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
