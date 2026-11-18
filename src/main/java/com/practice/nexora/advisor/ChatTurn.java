package com.practice.nexora.advisor;

public record ChatTurn(ChatRole role, String text) {

    static ChatTurn from(AdvisorMessage message) {
        return new ChatTurn(message.getRole(), message.getContent());
    }
}
