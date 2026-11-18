package com.practice.nexora.advisor;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiAdvisorClientTest {

    @Test
    void withoutHistoryTheModelGetsJustTheMessage() {
        assertThat(OpenAiAdvisorClient.withHistory(List.of(), "I need a laptop"))
                .isEqualTo("I need a laptop");
    }
    @Test
    void theHistoryBecomesALabelledTranscriptOldestFirst() {
        List<ChatTurn> history = List.of(
                new ChatTurn(ChatRole.USER, "I need a laptop"),
                new ChatTurn(ChatRole.ASSISTANT, "What is your budget")
        );

        String prompt = OpenAiAdvisorClient.withHistory(history, "About 900 euros, for university");

        assertThat(prompt)
                .contains("Customer: I need a laptop\nNexa: What is your budget")
                .endsWith("Customer's new message:\nAbout 900 euros, for university");
    }
}
