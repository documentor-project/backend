package com.documentor.backend.infra.mail;

import com.documentor.backend.domain.notification.NotificationSetting;
import com.documentor.backend.domain.question.Question;
import com.documentor.backend.service.notification.ReviewEmailSender;
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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(setting.getEmail());
        message.setSubject("[DocuMentor] 오늘의 복습 질문");
        message.setText(createBody(setting, questions));

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new IllegalStateException("이메일 발송에 실패했습니다.", exception);
        }
    }

    private String createBody(NotificationSetting setting, List<Question> questions) {
        StringBuilder body = new StringBuilder();
        body.append("오늘의 DocuMentor 복습 질문입니다.\n\n");
        body.append("질문 리스트: ").append(setting.getQuestionSet().getTitle()).append("\n");
        body.append("문서: ").append(setting.getQuestionSet().getDocument().getTitle()).append("\n\n");

        for (int index = 0; index < questions.size(); index++) {
            Question question = questions.get(index);
            body.append(index + 1)
                    .append(". ")
                    .append(question.getContent())
                    .append("\n");

            if (question.getSource() != null && question.getSource().getSnippet() != null) {
                body.append("   출처: ")
                        .append(question.getSource().getSnippet())
                        .append("\n");
            }
            body.append("\n");
        }

        if (questions.isEmpty()) {
            body.append("아직 발송할 질문이 없습니다.\n");
        }

        return body.toString();
    }
}
