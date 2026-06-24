package com.documentor.backend.service.notification;

import com.documentor.backend.domain.notification.NotificationSetting;
import com.documentor.backend.domain.question.Question;
import java.util.List;

public interface ReviewEmailSender {

    void send(NotificationSetting setting, List<Question> questions);

    void send(String recipient, String questionSetTitle, String documentTitle, List<ReviewEmailQuestion> questions);

    record ReviewEmailQuestion(String content, String sourceSnippet) {
    }
}
