package com.practice.nexora.user;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class TokenServiceTest {

    private static SecretKey key(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static JwtDecoder decoderFor(SecretKey key) {
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private final SecretKey serverKey = key("test-secret-that-is-long-enough-0123456789");
    private final TokenService tokens = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(serverKey)));
    private final UserAccount george = new UserAccount("george@example.com", "some-hash", Role.USER);

    @Test
    void theTokenSaysWhoAndUntilWhen() {
        TokenResponse response = tokens.issueFor(george);

        Jwt jwt = decoderFor(serverKey).decode(response.accessToken());
        assertThat(jwt.getSubject()).isEqualTo("george@example.com");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("USER");
        assertThat(jwt.getExpiresAt()).isCloseTo(Instant.now().plus(Duration.ofHours(1)), within(Duration.ofMinutes(1)));
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void aTokenSealedWithAnotherKeyIsRefused() {
        String token = tokens.issueFor(george).accessToken();
        JwtDecoder otherServer = decoderFor(key("a-completely-different-secret-9876543210"));

        assertThatThrownBy(() -> otherServer.decode(token)).isInstanceOf(JwtException.class);
    }
}
