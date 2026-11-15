package com.practice.nexora.user;


import com.practice.nexora.common.NexoraProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final Duration lifetime;


    @Autowired
    public TokenService(JwtEncoder encoder, NexoraProperties properties) {
        this(encoder, properties.jwt().ttl());
    }

    TokenService(JwtEncoder encoder, Duration lifetime) {
        this.encoder = encoder;
        this.lifetime = lifetime;
    }

    public TokenResponse issueFor(UserAccount account) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("nexora-store")
                .subject(account.getEmail())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim("roles", List.of(account.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", lifetime.toSeconds());
    }
}
