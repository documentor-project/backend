package com.documentor.backend.infra.mail;

import com.documentor.backend.domain.notification.NotificationSetting;
import com.documentor.backend.domain.question.Question;
import com.documentor.backend.service.notification.ReviewEmailSender;
import com.documentor.backend.service.notification.ReviewEmailSender.ReviewEmailQuestion;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject("[DocuMentor] \uc624\ub298\uc758 \ubcf5\uc2b5 \uc9c8\ubb38");
            helper.setText(createPlainTextBody(questionSetTitle, documentTitle, questions), createHtmlBody(questionSetTitle, documentTitle, questions));
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new IllegalStateException("\uc774\uba54\uc77c \ubc1c\uc1a1\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.", exception);
        }
    }

    private String createPlainTextBody(String questionSetTitle, String documentTitle, List<ReviewEmailQuestion> questions) {
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

    private String createHtmlBody(String questionSetTitle, String documentTitle, List<ReviewEmailQuestion> questions) {
        StringBuilder questionsHtml = new StringBuilder();
        for (int index = 0; index < questions.size(); index++) {
            ReviewEmailQuestion question = questions.get(index);
            questionsHtml.append("""
                    <tr>
                      <td style="padding:16px 0;border-top:1px solid #e5e7eb;">
                        <div style="font-size:12px;font-weight:700;color:#2563eb;letter-spacing:0.08em;text-transform:uppercase;margin-bottom:8px;">Question %d</div>
                        <div style="font-size:16px;line-height:1.65;color:#111827;font-weight:700;">%s</div>
                    """.formatted(index + 1, escape(question.content())));

            if (question.sourceSnippet() != null && !question.sourceSnippet().isBlank()) {
                questionsHtml.append("""
                        <div style="margin-top:12px;padding:12px 14px;background:#f8fafc;border-left:4px solid #38bdf8;border-radius:6px;color:#475569;font-size:13px;line-height:1.55;">
                          <strong style="color:#0f172a;">\ucd9c\ucc98</strong><br>%s
                        </div>
                        """.formatted(escape(question.sourceSnippet())));
            }

            questionsHtml.append("""
                      </td>
                    </tr>
                    """);
        }

        if (questions.isEmpty()) {
            questionsHtml.append("""
                    <tr>
                      <td style="padding:18px 0;border-top:1px solid #e5e7eb;color:#64748b;font-size:15px;">
                        \uc544\uc9c1 \ubc1c\uc1a1\ud560 \uc9c8\ubb38\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.
                      </td>
                    </tr>
                    """);
        }

        return """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>DocuMentor Review</title>
                </head>
                <body style="margin:0;padding:0;background:#f1f5f9;font-family:Arial,'Apple SD Gothic Neo','Malgun Gothic',sans-serif;color:#111827;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f1f5f9;padding:28px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:640px;background:#ffffff;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;">
                          <tr>
                            <td style="background:#0f172a;padding:28px 30px;">
                              <div style="font-size:13px;font-weight:700;color:#93c5fd;letter-spacing:0.12em;text-transform:uppercase;">DocuMentor</div>
                              <h1 style="margin:8px 0 0;font-size:24px;line-height:1.35;color:#ffffff;">\uc624\ub298\uc758 \ubcf5\uc2b5 \uc9c8\ubb38</h1>
                              <p style="margin:10px 0 0;color:#cbd5e1;font-size:14px;line-height:1.6;">\uc9e7\uac8c \ub418\uc0c8\uae30\uace0, \uc624\ub798 \uae30\uc5b5\ud558\ub294 \ub370 \ud544\uc694\ud55c \uc9c8\ubb38\uc785\ub2c8\ub2e4.</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 30px 4px;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;">
                                <tr>
                                  <td style="padding:16px 18px;">
                                    <div style="font-size:12px;color:#64748b;margin-bottom:6px;">\uc9c8\ubb38 \ub9ac\uc2a4\ud2b8</div>
                                    <div style="font-size:16px;font-weight:700;color:#0f172a;line-height:1.45;">%s</div>
                                    <div style="font-size:12px;color:#64748b;margin:14px 0 6px;">\ubb38\uc11c</div>
                                    <div style="font-size:14px;color:#334155;line-height:1.45;">%s</div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 30px 28px;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                %s
                              </table>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(escape(questionSetTitle), escape(documentTitle), questionsHtml);
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
