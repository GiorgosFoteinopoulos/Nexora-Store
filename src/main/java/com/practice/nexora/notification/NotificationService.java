package com.practice.nexora.notification;


import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notifications;
    private final UserAccountRepository users;

    public NotificationService(NotificationRepository notifications, UserAccountRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> notificationsOf(String email) {
        return notifications.findByUserOrderByCreatedAtDescIdDesc(findUser(email)).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public void markAllRead(String email) {
        notifications.markAllRead(findUser(email), Instant.now());
    }

    private UserAccount findUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }
}
