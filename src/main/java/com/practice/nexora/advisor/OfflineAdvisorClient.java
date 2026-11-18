package com.practice.nexora.advisor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "nexora.advisor", name = "provider", havingValue = "offline")
class OfflineAdvisorClient implements AdvisorClient {

    static final String REPLY = "Nexa is offline in this environment. Please browse the catalog in the meantime.";

    @Override
    public NexaAnswer reply(String userMessage) {
        return new NexaAnswer(REPLY, List.of());
    }
}
