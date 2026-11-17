package com.practice.nexora.notification;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PriceDropAlerts {

    private static final Logger log = LoggerFactory.getLogger(PriceDropAlerts.class);

    private final NotificationRepository notifications;

    public PriceDropAlerts(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional
    public int createPriceDropNotifications(Instant since) {
        int created = notifications.createPriceDropNotifications(since);
        if (created > 0) {
            log.info("Created {} price-drop notification(s)", created);
        }
        return created;
    }
}
