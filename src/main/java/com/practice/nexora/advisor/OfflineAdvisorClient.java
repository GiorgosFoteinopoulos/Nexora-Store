package com.practice.nexora.advisor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "nexora.advisor", name = "provider", havingValue = "offline")
class OfflineAdvisorClient implements AdvisorClient {

    static final String REPLY = "Nexa is offline in this environment. Please browse the catalog in the meantime.";

    @Override
    public String reply(String userMessage) {
        return REPLY;
    }
}
