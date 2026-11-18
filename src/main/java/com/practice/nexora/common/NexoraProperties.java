package com.practice.nexora.common;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.aop.Advisor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "nexora")
public record NexoraProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @DefaultValue Admin admin,
        @Valid @NotNull Cors cors,
        @Valid @DefaultValue Alerts alerts,
        @Valid @DefaultValue Advisor advisor) {

    public record Jwt(@NotBlank String secret, @DefaultValue("1h") Duration ttl) {

        public Jwt {
            if (secret != null && secret.getBytes(StandardCharsets.UTF_8).length < 32) {
                throw new IllegalArgumentException("nexora.jwt.secret must be at least 32 bytes for HS256");
            }
        }

        @Override
        public String toString() {
            return "Jwt[secret=****, ttl=" + ttl + "]";
        }
    }

    public record Admin(String email, String password) {

        public boolean isConfigured() {
            return email != null && !email.isBlank()
                    && password != null && !password.isBlank();
        }

        @Override
        public String toString() {
            return "Admin[email=" + email + ", pasword=****]";
        }
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {

    }

    public record Alerts(@DefaultValue("7d") Duration lookback) {

        public Alerts {
            if (lookback != null && (lookback.isZero()) || lookback.isNegative()) {
                throw new IllegalArgumentException("nexora.alerts.look-back must be positive");
            }
        }
    }

    public record Advisor(@DefaultValue("30s") Duration timeout) {
        public Advisor {
            if (timeout != null && (timeout.isZero() || timeout.isNegative())) {
                throw new IllegalArgumentException("nexora.advisor.timeout must be positive");
            }
        }
    }
}
