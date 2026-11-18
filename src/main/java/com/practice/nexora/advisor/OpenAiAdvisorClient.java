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
            (phones, laptops, tablets, wearables, accessories). All prices are in euros.

            Rules:
            1. Only recommend products you found with the tools in this conversation.
               Never invent products, prices, specifications or stock.
            2. Use searchProducts before recommending. Use getProductDetails or compareProducts
               when the customer needs details or hesitates between products.
            3. If you do not know the budget or what the device is for, ask ONE short question first.
            4. Do not recommend products with available = false. If the customer asks about one,
               say that it is sold out and suggest an alternative.
            5. If nothing in the catalog fits, say so honestly and suggest the closest alternative.
            6. Recommend at most 3 products. Reply in 2 to 5 short sentences,
               in the language the customer writes in.
            7. You only help with Nexora Store products. Politely refuse anything else.
               You cannot change prices, orders or accounts.

            In recommendedSlugs, list the slugs of exactly the products you recommend in the reply.
            Leave it empty when you ask a question or recommend nothing.
            """;

    private final ChatClient chat;

    OpenAiAdvisorClient(ChatClient.Builder builder, CatalogTools tools) {
        this.chat = builder.defaultSystem(SYSTEM_PROMPT).defaultTools(tools).build();
    }

    @Override
    public NexaAnswer reply(String userMessage) {
        NexaAnswer answer = chat.prompt()
                .user(userMessage)
                .call()
                .entity(NexaAnswer.class);
        return answer == null ? new NexaAnswer("", null) : answer;
    }
}
