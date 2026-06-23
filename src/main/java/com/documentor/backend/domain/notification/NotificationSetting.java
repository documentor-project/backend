package com.documentor.backend.domain.notification;

import com.documentor.backend.domain.common.BaseEntity;
import com.documentor.backend.domain.question.QuestionSet;
import com.documentor.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "notification_settings")
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_set_id")
    private QuestionSet questionSet;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalTime sendTime;

    @Column(nullable = false)
    private int questionCount;

    protected NotificationSetting() {
    }

    private NotificationSetting(User user) {
        this.user = user;
        this.enabled = false;
        this.email = user.getEmail();
        this.sendTime = LocalTime.of(9, 0);
        this.questionCount = 3;
    }

    public static NotificationSetting createDefault(User user) {
        return new NotificationSetting(user);
    }

    public void update(boolean enabled, String email, LocalTime sendTime, int questionCount, QuestionSet questionSet) {
        this.enabled = enabled;
        this.email = email;
        this.sendTime = sendTime;
        this.questionCount = questionCount;
        this.questionSet = questionSet;
    }

    public boolean shouldSendAt(LocalTime now) {
        return enabled
                && questionSet != null
                && sendTime.getHour() == now.getHour()
                && sendTime.getMinute() == now.getMinute();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public QuestionSet getQuestionSet() {
        return questionSet;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getEmail() {
        return email;
    }

    public LocalTime getSendTime() {
        return sendTime;
    }

    public int getQuestionCount() {
        return questionCount;
    }

}
