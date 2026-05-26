package com.documentor.backend.domain.question;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_set_id", nullable = false)
    private QuestionSet questionSet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionDifficulty difficulty;

    @Column(nullable = false, length = 1000)
    private String content;

    @Embedded
    private QuestionSource source;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<FollowUpQuestion> followUps = new ArrayList<>();

    @Column(nullable = false)
    private boolean bookmarked;

    @Column(nullable = false)
    private boolean answered;

    private LocalDateTime answeredAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Question() {
    }

    private Question(QuestionSet questionSet, QuestionType type, QuestionDifficulty difficulty, String content, QuestionSource source) {
        this.questionSet = questionSet;
        this.type = type;
        this.difficulty = difficulty;
        this.content = content;
        this.source = source;
        this.createdAt = LocalDateTime.now();
    }

    public static Question create(QuestionSet questionSet, QuestionType type, QuestionDifficulty difficulty, String content, QuestionSource source) {
        return new Question(questionSet, type, difficulty, content, source);
    }

    public void addFollowUp(String content) {
        followUps.add(FollowUpQuestion.create(this, content));
    }

    public void updateBookmark(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public void updateAnswered(boolean answered) {
        this.answered = answered;
        this.answeredAt = answered ? LocalDateTime.now() : null;
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

    public QuestionType getType() {
        return type;
    }

    public QuestionDifficulty getDifficulty() {
        return difficulty;
    }

    public String getContent() {
        return content;
    }

    public QuestionSource getSource() {
        return source;
    }

    public List<FollowUpQuestion> getFollowUps() {
        return Collections.unmodifiableList(followUps);
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public boolean isAnswered() {
        return answered;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
