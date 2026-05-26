package com.documentor.backend.infra.notification;

import com.documentor.backend.domain.notification.ReviewDelivery;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewDeliveryRepository extends JpaRepository<ReviewDelivery, Long> {

    boolean existsByUserIdAndQuestionSetIdAndDeliveryDate(Long userId, Long questionSetId, LocalDate deliveryDate);

    Page<ReviewDelivery> findAllByUserId(Long userId, Pageable pageable);
}
