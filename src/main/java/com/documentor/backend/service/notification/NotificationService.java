package com.documentor.backend.service.notification;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.notification.NotificationSetting;
import com.documentor.backend.domain.notification.ReviewDelivery;
import com.documentor.backend.domain.question.Question;
import com.documentor.backend.domain.question.QuestionSet;
import com.documentor.backend.domain.user.User;
import com.documentor.backend.infra.notification.NotificationSettingRepository;
import com.documentor.backend.infra.notification.ReviewDeliveryRepository;
import com.documentor.backend.infra.question.QuestionSetRepository;
import com.documentor.backend.infra.security.AuthenticatedUserResolver;
import com.documentor.backend.infra.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final ReviewDeliveryRepository reviewDeliveryRepository;
    private final UserRepository userRepository;
    private final QuestionSetRepository questionSetRepository;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ReviewEmailSender reviewEmailSender;

    public NotificationService(
            NotificationSettingRepository notificationSettingRepository,
            ReviewDeliveryRepository reviewDeliveryRepository,
            UserRepository userRepository,
            QuestionSetRepository questionSetRepository,
            AuthenticatedUserResolver authenticatedUserResolver,
            ReviewEmailSender reviewEmailSender
    ) {
        this.notificationSettingRepository = notificationSettingRepository;
        this.reviewDeliveryRepository = reviewDeliveryRepository;
        this.userRepository = userRepository;
        this.questionSetRepository = questionSetRepository;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.reviewEmailSender = reviewEmailSender;
    }

    public NotificationSettingResult getMySetting(String authorizationHeader) {
        User user = getAuthenticatedUser(authorizationHeader);
        NotificationSetting setting = notificationSettingRepository.findByUserId(user.getId())
                .orElseGet(() -> notificationSettingRepository.save(NotificationSetting.createDefault(user)));
        return NotificationSettingResult.from(setting);
    }

    public NotificationSettingResult saveMySetting(String authorizationHeader, NotificationSettingCommand command) {
        User user = getAuthenticatedUser(authorizationHeader);
        QuestionSet questionSet = null;
        if (command.questionSetId() != null) {
            questionSet = getOwnedQuestionSet(user.getId(), command.questionSetId());
        }
        if (command.enabled() && (command.email() == null || command.email().isBlank())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "알림을 켜려면 이메일이 필요합니다.");
        }

        NotificationSetting setting = notificationSettingRepository.findByUserId(user.getId())
                .orElseGet(() -> NotificationSetting.createDefault(user));
        setting.update(command.enabled(), command.email(), command.sendTime(), command.questionCount(), questionSet);
        return NotificationSettingResult.from(notificationSettingRepository.save(setting));
    }

    public Page<ReviewDeliveryResult> getReviewDeliveries(String authorizationHeader, Pageable pageable) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        return reviewDeliveryRepository.findAllByUserId(userId, pageable)
                .map(ReviewDeliveryResult::from);
    }

    public TestReviewEmailResult sendTestReviewEmail(String authorizationHeader) {
        authenticatedUserResolver.resolve(authorizationHeader);
        String recipient = "jumdo12@gmail.com";
        List<ReviewEmailSender.ReviewEmailQuestion> questions = List.of(
                new ReviewEmailSender.ReviewEmailQuestion(
                        "\uc2a4\ud504\ub9c1\uc5d0\uc11c @Transactional\uc774 \uc801\uc6a9\ub418\ub294 \uae30\ubcf8 \uc6d0\ub9ac\ub294 \ubb34\uc5c7\uc778\uac00\uc694?",
                        "Spring AOP proxy intercepts method calls and manages transaction boundaries."
                ),
                new ReviewEmailSender.ReviewEmailQuestion(
                        "\ubb38\uc11c \ud30c\uc2f1, \uc784\ubca0\ub529, READY \uc0c1\ud0dc\ub294 \uac01\uac01 \uc5b8\uc81c \uc0ac\uc6a9\ub418\ub098\uc694?",
                        "Document status flows from UPLOADED to PARSING, EMBEDDING, and READY."
                ),
                new ReviewEmailSender.ReviewEmailQuestion(
                        "\ubcf5\uc2b5 \uc54c\ub9bc\uc740 \uc5b4\ub5a4 \uc870\uac74\uc5d0\uc11c \ud558\ub8e8 \ud55c \ubc88\ub9cc \ubc1c\uc1a1\ub418\ub098\uc694?",
                        "Notification settings are checked every minute and duplicate daily deliveries are skipped."
                )
        );

        reviewEmailSender.send(
                recipient,
                "SMTP \ud14c\uc2a4\ud2b8 \ubcf5\uc2b5 \uc9c8\ubb38",
                "DocuMentor \ud14c\uc2a4\ud2b8 \ubb38\uc11c",
                questions
        );
        return new TestReviewEmailResult(recipient, questions.size());
    }

    @Transactional
    public void sendDueReviewQuestions() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        notificationSettingRepository.findAll()
                .stream()
                .filter(setting -> setting.shouldSendAt(now))
                .filter(setting -> !alreadyDeliveredToday(setting))
                .forEach(this::sendAndRecordDelivery);
    }

    private boolean alreadyDeliveredToday(NotificationSetting setting) {
        return reviewDeliveryRepository.existsByUserIdAndQuestionSetIdAndDeliveryDate(
                setting.getUser().getId(),
                setting.getQuestionSet().getId(),
                LocalDate.now()
        );
    }

    private void sendAndRecordDelivery(NotificationSetting setting) {
        List<Question> questions = setting.getQuestionSet()
                .getQuestions()
                .stream()
                .limit(setting.getQuestionCount())
                .toList();

        try {
            reviewEmailSender.send(setting, questions);
            reviewDeliveryRepository.save(ReviewDelivery.sent(
                    setting.getUser(),
                    setting.getQuestionSet(),
                    setting.getEmail(),
                    questions.size()
            ));
        } catch (RuntimeException exception) {
            reviewDeliveryRepository.save(ReviewDelivery.failed(
                    setting.getUser(),
                    setting.getQuestionSet(),
                    setting.getEmail(),
                    questions.size(),
                    exception.getMessage()
            ));
        }
    }

    private User getAuthenticatedUser(String authorizationHeader) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
    }

    private QuestionSet getOwnedQuestionSet(Long userId, Long questionSetId) {
        QuestionSet questionSet = questionSetRepository.findById(questionSetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "질문 리스트를 찾을 수 없습니다."));
        if (!questionSet.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "질문 리스트에 접근할 수 없습니다.");
        }
        return questionSet;
    }
}
