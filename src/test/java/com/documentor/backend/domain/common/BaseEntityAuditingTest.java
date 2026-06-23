package com.documentor.backend.domain.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.documentor.backend.domain.document.TechnicalDocument;
import com.documentor.backend.domain.notification.NotificationSetting;
import com.documentor.backend.domain.notification.ReviewDelivery;
import com.documentor.backend.domain.question.FollowUpQuestion;
import com.documentor.backend.domain.question.Question;
import com.documentor.backend.domain.question.QuestionSet;
import com.documentor.backend.domain.share.ShareLink;
import com.documentor.backend.domain.user.User;
import com.documentor.backend.infra.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class BaseEntityAuditingTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void allEntitiesInheritBaseEntity() {
        List<Class<?>> entityTypes = List.of(
                User.class,
                TechnicalDocument.class,
                QuestionSet.class,
                Question.class,
                FollowUpQuestion.class,
                ShareLink.class,
                NotificationSetting.class,
                ReviewDelivery.class
        );

        assertThat(entityTypes).allMatch(BaseEntity.class::isAssignableFrom);
    }

    @Test
    void createdAtAndUpdatedAtAreAutomaticallySetWhenEntityIsPersisted() {
        User user = User.create("auditing@example.com", "encoded-password", "감사테스트");

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isEqualTo(savedUser.getCreatedAt());
    }

    @Test
    void updatedAtIsAutomaticallyChangedWhenEntityIsUpdated() throws InterruptedException {
        User user = userRepository.saveAndFlush(
                User.create("update-auditing@example.com", "encoded-password", "변경전")
        );
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime firstUpdatedAt = user.getUpdatedAt();

        Thread.sleep(10);
        user.updateNickname("변경후");
        userRepository.saveAndFlush(user);
        entityManager.clear();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThat(updatedUser.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updatedUser.getUpdatedAt()).isAfter(firstUpdatedAt);
        assertThat(updatedUser.getNickname()).isEqualTo("변경후");
    }
}
