package com.documentor.backend.service.notification;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.notification.NotificationSetting;
import com.documentor.backend.domain.notification.ReviewDelivery;
import com.documentor.backend.domain.question.QuestionSet;
import com.documentor.backend.domain.user.User;
import com.documentor.backend.infra.notification.NotificationSettingRepository;
import com.documentor.backend.infra.notification.ReviewDeliveryRepository;
import com.documentor.backend.infra.question.QuestionSetRepository;
import com.documentor.backend.infra.security.AuthenticatedUserResolver;
import com.documentor.backend.infra.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final ReviewDeliveryRepository reviewDeliveryRepository;
    private final UserRepository userRepository;
    private final QuestionSetRepository questionSetRepository;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public NotificationService(
            NotificationSettingRepository notificationSettingRepository,
            ReviewDeliveryRepository reviewDeliveryRepository,
            UserRepository userRepository,
            QuestionSetRepository questionSetRepository,
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        this.notificationSettingRepository = notificationSettingRepository;
        this.reviewDeliveryRepository = reviewDeliveryRepository;
        this.userRepository = userRepository;
        this.questionSetRepository = questionSetRepository;
        this.authenticatedUserResolver = authenticatedUserResolver;
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

    public void sendDueReviewQuestions() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        notificationSettingRepository.findAll()
                .stream()
                .filter(setting -> setting.shouldSendAt(now))
                .filter(setting -> !alreadyDeliveredToday(setting))
                .forEach(this::recordSentDelivery);
    }

    private boolean alreadyDeliveredToday(NotificationSetting setting) {
        return reviewDeliveryRepository.existsByUserIdAndQuestionSetIdAndDeliveryDate(
                setting.getUser().getId(),
                setting.getQuestionSet().getId(),
                LocalDate.now()
        );
    }

    private void recordSentDelivery(NotificationSetting setting) {
        ReviewDelivery delivery = ReviewDelivery.sent(
                setting.getUser(),
                setting.getQuestionSet(),
                setting.getEmail(),
                setting.getQuestionCount()
        );
        reviewDeliveryRepository.save(delivery);
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
