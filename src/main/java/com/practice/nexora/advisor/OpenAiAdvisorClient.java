package com.practice.nexora.advisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "nexora.advisor", name = "provider", havingValue = "openai")
class OpenAiAdvisorClient implements AdvisorClient {

    static final String SYSTEM_PROMPT = """
            You are Nexa, the shopping advisor of Nexora Store, a consumer-electronics shop
            (phones, laptops, tablets, wearables, accessories).
            Answer in 2 to 4 short sentences, in the language the customer writes in.
            You do NOT have access to the product catalog yet: never name specific products, prices or stock.
            If you are asked for them, say that you will soon be able to look them up,
            and ask about the customer's budget and what they will use the device for.
            """;

    private final ChatClient chat;

    OpenAiAdvisorClient(ChatClient.Builder builder) {
        this.chat = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public String reply(String userMessage) {
        String content = chat.prompt()
                .user(userMessage)
                .call()
                .content();
        return Objects.requireNonNullElse(content, "");
    }
}
