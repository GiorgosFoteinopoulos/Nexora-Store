package com.practice.nexora.notification;


import com.practice.nexora.common.NexoraProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
@ConditionalOnProperty(prefix = "nexora.alerts", name = "enabled", havingValue = "true", matchIfMissing = true)
class PriceDropAlertJob {

    private final PriceDropAlerts alerts;
    private final Duration lookback;
    private final Clock clock;

    @Autowired
    PriceDropAlertJob(PriceDropAlerts alerts, NexoraProperties properties, Clock clock) {
        this(alerts, properties.alerts().lookback(), clock);
    }


    PriceDropAlertJob(PriceDropAlerts alerts, Duration lookback, Clock clock) {
        this.alerts = alerts;
        this.lookback = lookback;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${nexora.alerts.interval:PT5M}",
               initialDelayString = "${nexora.alerts.initial-delay:PT30S}")
    void run() {
        Instant since = Instant.now(clock).minus(lookback);
        alerts.createPriceDropNotifications(since);
    }
}
