package com.practice.nexora.advisor;

import java.time.Instant;

public record ChatTurnResponse(ChatRole role, String text, Instant createdAt) {

    static ChatTurnResponse from(AdvisorMessage message) {
        return new ChatTurnResponse(message.getRole(), message.getContent(), message.getCreatedAt());
    }
}
