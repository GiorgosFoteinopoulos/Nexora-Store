package com.practice.nexora.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceDropAlertJobTest {

    @Mock
    private PriceDropAlerts alerts;

    @Test
    void looksBackExactlyTheConfiguredTime() {
        Clock fixed = Clock.fixed(Instant.parse("2026-11-17T12:00:00Z"), ZoneOffset.UTC);
        PriceDropAlertJob job = new PriceDropAlertJob(alerts, Duration.ofDays(7), fixed);

        job.run();

        verify(alerts).createPriceDropNotifications(Instant.parse("2026-11-10T12:00:00Z"));
    }
}