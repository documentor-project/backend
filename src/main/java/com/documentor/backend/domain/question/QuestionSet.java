package com.documentor.backend.domain.question;

import com.documentor.backend.domain.document.TechnicalDocument;
import com.documentor.backend.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "question_sets")
public class QuestionSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private TechnicalDocument document;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionDifficulty difficulty;

    @OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Question> questions = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected QuestionSet() {
    }

    private QuestionSet(User owner, TechnicalDocument document, String title, QuestionDifficulty difficulty) {
        LocalDateTime now = LocalDateTime.now();
        this.owner = owner;
        this.document = document;
        this.title = title;
        this.difficulty = difficulty;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static QuestionSet create(User owner, TechnicalDocument document, String title, QuestionDifficulty difficulty) {
        return new QuestionSet(owner, document, title, difficulty);
    }

    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long userId) {
        return owner.getId().equals(userId);
    }

    public Long getId() {
        return id;
    }

    public TechnicalDocument getDocument() {
        return document;
    }

    public String getTitle() {
        return title;
    }

    public QuestionDifficulty getDifficulty() {
        return difficulty;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
