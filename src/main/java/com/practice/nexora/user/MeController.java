package com.practice.nexora.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Accounts")
@RestController
class MeController {

    record MeResponse(String email, List<String> roles) {

    }

    @Operation(summary = "Who am I?", security = @SecurityRequirement(name = "bearer"))
    @GetMapping("/api/me")
    MeResponse me(@AuthenticationPrincipal Jwt token) {
        return new MeResponse(token.getSubject(), token.getClaimAsStringList("roles"));
    }
}
