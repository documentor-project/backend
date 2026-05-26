package com.documentor.backend.presentation.notification;

import com.documentor.backend.presentation.document.PageResponse;
import com.documentor.backend.service.notification.NotificationService;
import com.documentor.backend.service.notification.ReviewDeliveryResult;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notification-settings/me")
    public NotificationSettingResponse getMyNotificationSetting(@RequestHeader("Authorization") String authorizationHeader) {
        return NotificationSettingResponse.from(notificationService.getMySetting(authorizationHeader));
    }

    @PutMapping("/notification-settings/me")
    public NotificationSettingResponse saveMyNotificationSetting(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody SaveNotificationSettingRequest request
    ) {
        return NotificationSettingResponse.from(notificationService.saveMySetting(authorizationHeader, request.toCommand()));
    }

    @GetMapping("/review-deliveries")
    public PageResponse<ReviewDeliveryResponse> getReviewDeliveries(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReviewDeliveryResult> result = notificationService.getReviewDeliveries(authorizationHeader, PageRequest.of(page, size));
        return PageResponse.from(result.map(ReviewDeliveryResponse::from));
    }
}
