package com.documentor.backend.domain.notification;

import com.documentor.backend.domain.question.QuestionSet;
import com.documentor.backend.domain.user.User;
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
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_deliveries")
public class ReviewDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_set_id", nullable = false)
    private QuestionSet questionSet;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int questionCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(nullable = false)
    private LocalDate deliveryDate;

    private LocalDateTime sentAt;

    @Column(length = 1000)
    private String failureReason;

    protected ReviewDelivery() {
    }

    private ReviewDelivery(User user, QuestionSet questionSet, String email, int questionCount, DeliveryStatus status, String failureReason) {
        this.user = user;
        this.questionSet = questionSet;
        this.email = email;
        this.questionCount = questionCount;
        this.status = status;
        this.deliveryDate = LocalDate.now();
        this.sentAt = status == DeliveryStatus.SENT ? LocalDateTime.now() : null;
        this.failureReason = failureReason;
    }

    public static ReviewDelivery sent(User user, QuestionSet questionSet, String email, int questionCount) {
        return new ReviewDelivery(user, questionSet, email, questionCount, DeliveryStatus.SENT, null);
    }

    public static ReviewDelivery failed(User user, QuestionSet questionSet, String email, int questionCount, String failureReason) {
        return new ReviewDelivery(user, questionSet, email, questionCount, DeliveryStatus.FAILED, failureReason);
    }

    public Long getId() {
        return id;
    }

    public QuestionSet getQuestionSet() {
        return questionSet;
    }

    public String getEmail() {
        return email;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
