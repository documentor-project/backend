package com.documentor.backend.infra.mail;

import com.documentor.backend.domain.notification.NotificationSetting;
import com.documentor.backend.domain.question.Question;
import com.documentor.backend.service.notification.ReviewEmailSender;
import com.documentor.backend.service.notification.ReviewEmailSender.ReviewEmailQuestion;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpReviewEmailSender implements ReviewEmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpReviewEmailSender(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(NotificationSetting setting, List<Question> questions) {
        List<ReviewEmailQuestion> emailQuestions = questions.stream()
                .map(question -> new ReviewEmailQuestion(
                        question.getContent(),
                        question.getSource() == null ? null : question.getSource().getSnippet()
                ))
                .toList();

        send(
                setting.getEmail(),
                setting.getQuestionSet().getTitle(),
                setting.getQuestionSet().getDocument().getTitle(),
                emailQuestions
        );
    }

    @Override
    public void send(String recipient, String questionSetTitle, String documentTitle, List<ReviewEmailQuestion> questions) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("[DocuMentor] \uc624\ub298\uc758 \ubcf5\uc2b5 \uc9c8\ubb38");
        message.setText(createBody(questionSetTitle, documentTitle, questions));

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new IllegalStateException("\uc774\uba54\uc77c \ubc1c\uc1a1\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.", exception);
        }
    }

    private String createBody(String questionSetTitle, String documentTitle, List<ReviewEmailQuestion> questions) {
        StringBuilder body = new StringBuilder();
        body.append("\uc624\ub298\uc758 DocuMentor \ubcf5\uc2b5 \uc9c8\ubb38\uc785\ub2c8\ub2e4.\n\n");
        body.append("\uc9c8\ubb38 \ub9ac\uc2a4\ud2b8: ").append(questionSetTitle).append("\n");
        body.append("\ubb38\uc11c: ").append(documentTitle).append("\n\n");

        for (int index = 0; index < questions.size(); index++) {
            ReviewEmailQuestion question = questions.get(index);
            body.append(index + 1)
                    .append(". ")
                    .append(question.content())
                    .append("\n");

            if (question.sourceSnippet() != null && !question.sourceSnippet().isBlank()) {
                body.append("   \ucd9c\ucc98: ")
                        .append(question.sourceSnippet())
                        .append("\n");
            }
            body.append("\n");
        }

        if (questions.isEmpty()) {
            body.append("\uc544\uc9c1 \ubc1c\uc1a1\ud560 \uc9c8\ubb38\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.\n");
        }

        return body.toString();
    }
}
