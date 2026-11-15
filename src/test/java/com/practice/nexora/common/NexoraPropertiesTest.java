package com.practice.nexora.common;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
class NexoraPropertiesTest {

    private static final String GOOD_SECRET = "test-secret-that-is-long-enough-0123456789";

    @EnableConfigurationProperties(NexoraProperties.class)
    static class PropertiesOnly {

    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesOnly.class)
            .withPropertyValues(
                    "nexora.jwt.secret=" + GOOD_SECRET,
                    "nexora.cors.allowed-origins=http://localhost:5500");

    @Test
    void bindsTheValuesFromProperties() {
        runner.withPropertyValues(
                        "nexora.jwt.ttl=30m",
                        "nexora.admin.email=admin@nexora.local",
                        "nexora.admin.password=Secret123!",
                        "nexora.cors.allowed-origins=http://localhost:5500,http://127.0.0.1:5500")
                .run(context -> {
                    NexoraProperties properties = context.getBean(NexoraProperties.class);

                    assertThat(properties.jwt().secret()).isEqualTo(GOOD_SECRET);
                    assertThat(properties.jwt().ttl()).isEqualTo(Duration.ofMinutes(30));
                    assertThat(properties.admin().isConfigured()).isTrue();
                    assertThat(properties.cors().allowedOrigins())
                            .containsExactly("http://localhost:5500", "http://127.0.0.1:5500");
                });
    }

    @Test
    void ttlDefaultsToOneHour_andAdminIsOptional() {
        runner.run(context -> {
            NexoraProperties properties = context.getBean(NexoraProperties.class);

            assertThat(properties.jwt().ttl()).isEqualTo(Duration.ofHours(1));
            assertThat(properties.admin()).isNotNull();
            assertThat(properties.admin().isConfigured()).isFalse();
        });
    }

    @Test
    void aTooShortSecretStopsTheApp() {
        runner.withPropertyValues("nexora.jwt.secret=short")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .rootCause()
                            .hasMessageContaining("at least 32 bytes");
                });
    }

    @Test
    void noAllowedOriginsStopsTheApp() {
        runner.withPropertyValues("nexora.cors.allowed-origins=")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void toStringNeverPrintsSecrets() {
        var jwt = new NexoraProperties.Jwt(GOOD_SECRET, Duration.ofHours(1));
        var admin = new NexoraProperties.Admin("admin@nexora.local", "Secret123!");

        assertThat(jwt.toString()).doesNotContain(GOOD_SECRET).contains("****");
        assertThat(admin.toString()).doesNotContain("Secret123!").contains("admin@nexora.local");
    }
}
