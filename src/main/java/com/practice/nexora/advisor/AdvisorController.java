package com.practice.nexora.advisor;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return advisor.chat(request.message());
    }
}
