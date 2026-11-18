package com.practice.nexora.advisor;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advisor")
@Tag(name = "Nexa")
class AdvisorController {

    private final AdvisorService advisor;

    AdvisorController(AdvisorService advisor) {
        this.advisor = advisor;
    }
    @PostMapping("/chat")
    @Operation(summary = "Ask Nexa (logged-in users). Recommended products come back as verified cards.")
    ChatResponse chat(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChatRequest request) {
        return advisor.chat(jwt.getSubject(), request.message());
    }

    @GetMapping("/conversation")
    @Operation(summary = "My conversation with Nexa, oldest first")
    List<ChatTurnResponse> conversation(@AuthenticationPrincipal Jwt jwt) {
        return advisor.conversationOf(jwt.getSubject());
    }

    @DeleteMapping("/conversation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Forget my conversation with Nexa (idempotent)")
    void forget(@AuthenticationPrincipal Jwt jwt) {
        advisor.forget(jwt.getSubject());
    }
}
