package com.documentor.backend.domain.share;

import com.documentor.backend.domain.question.QuestionSet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "share_links")
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_set_id", nullable = false)
    private QuestionSet questionSet;

    @Column(nullable = false, unique = true, length = 80)
    private String token;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ShareLink() {
    }

    private ShareLink(QuestionSet questionSet, String token, LocalDateTime expiresAt) {
        this.questionSet = questionSet;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public static ShareLink create(QuestionSet questionSet, String token, LocalDateTime expiresAt) {
        return new ShareLink(questionSet, token, expiresAt);
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isOwnedBy(Long userId) {
        return questionSet.isOwnedBy(userId);
    }

    public Long getId() {
        return id;
    }

    public QuestionSet getQuestionSet() {
        return questionSet;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
