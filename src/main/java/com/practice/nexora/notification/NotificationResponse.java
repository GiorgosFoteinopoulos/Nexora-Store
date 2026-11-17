package com.practice.nexora.notification;

import java.time.Instant;

public record NotificationResponse(Long id, String productSlug, String message, Instant createdAt, boolean read) {

    static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getProductSlug(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}
