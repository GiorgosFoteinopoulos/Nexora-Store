package com.practice.nexora.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "nexora.advisor", name = "provider", havingValue = "openai")
class OpenAiAdvisorClient implements AdvisorClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiAdvisorClient.class);
    
    static final String SYSTEM_PROMPT = """
            You are Nexa, the shopping advisor of Nexora Store, a consumer-electronics shop
            (phones, laptops, tablets, wearables, accessories). All prices are in euros.

            Rules:
            1. Only recommend products you found with the tools in this conversation.
               Never invent products, prices, specifications or stock.
            2. To find products, use findProductsByMeaning when the customer describes a need in their own words,
               and searchProducts for exact filters or a single keyword (category, brand, budget).
               Use getProductDetails or compareProducts when the customer needs details or hesitates between products.
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
    public NexaAnswer reply(List<ChatTurn> history, String userMessage) {
        NexaAnswer answer = chat.prompt()
                .user(withHistory(history, userMessage))
                .call()
                .entity(NexaAnswer.class);
        return answer == null ? new NexaAnswer("", null) : answer;
    }

    static String withHistory(List<ChatTurn> history, String userMessage) {
        String message = oneLine(userMessage);
        if (history.isEmpty()) {
            return message;
        }
        String transcript = history.stream()
                .map(turn -> (turn.role() == ChatRole.USER ? "Customer: " : "Nexa: ") + oneLine(turn.text()))
                .collect(Collectors.joining("\n"));
        return """
                Conversation so far (oldest first):
                %s

                Customer's new message:
                %s""".formatted(transcript, message);
    }

    static String oneLine(String text) {
        return text == null ? "" : text.replaceAll("\\s*\\R\\s*", " ").strip();
    }

    private static void logUsage(org.springframework.ai.chat.model.ChatResponse response) {
        if (response == null || response.getMetadata() == null || response.getMetadata().getUsage() == null) {
            return;
        }
        Usage usage = response.getMetadata().getUsage();
        log.info("Nexa tokens: prompt={}, completion={}, total={}",
                usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
    }
}
