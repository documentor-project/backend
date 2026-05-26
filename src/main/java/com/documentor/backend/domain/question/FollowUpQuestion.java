package com.documentor.backend.domain.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "follow_up_questions")
public class FollowUpQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 1000)
    private String content;

    protected FollowUpQuestion() {
    }

    private FollowUpQuestion(Question question, String content) {
        this.question = question;
        this.content = content;
    }

    public static FollowUpQuestion create(Question question, String content) {
        return new FollowUpQuestion(question, content);
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
