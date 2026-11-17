package com.practice.nexora.notification;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/notifications")
@Tag(name = "Notifications")
class NotificationController {

    private final NotificationService notifications;

    NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    @Operation(summary = "My notifications, newest first")
    List<NotificationResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return notifications.notificationsOf(jwt.getSubject());
    }

    @PutMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark all my notifications as read (idempotent)")
    void markAllRead(@AuthenticationPrincipal Jwt jwt) {
        notifications.markAllRead(jwt.getSubject());
    }
}
