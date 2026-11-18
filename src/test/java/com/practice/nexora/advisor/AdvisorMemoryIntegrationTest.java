package com.practice.nexora.advisor;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.user.Role;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdvisorMemoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdvisorService advisorService;

    @Autowired
    private AdvisorMessageRepository messages;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private AdvisorClient advisor;

    /** What the scripted Nexa received as history on each call, in call order. */
    private final List<List<ChatTurn>> sentHistories = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() {
        users.save(new UserAccount("george@example.com", "some-hash", Role.USER));
        users.save(new UserAccount("maria@example.com", "some-hash", Role.USER));
        when(advisor.reply(anyList(), anyString())).thenAnswer(call -> {
            List<ChatTurn> history = call.getArgument(0);
            sentHistories.add(List.copyOf(history));
            return new NexaAnswer("Nexa answer to: " + call.getArgument(1), List.of());
        });
    }

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    private static RequestPostProcessor as(String email) {
        return jwt().jwt(token -> token.subject(email));
    }

    private void ask(String email, String message) throws Exception {
        mockMvc.perform(post("/api/advisor/chat").with(as(email))
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\": \"" + message + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void everyExchangeIsStoredInOrder() throws Exception {
        ask("george@example.com", "I need a laptop");
        ask("george@example.com", "About 900 euros");

        mockMvc.perform(get("/api/advisor/conversation").with(as("george@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].text").value("I need a laptop"))
                .andExpect(jsonPath("$[1].role").value("ASSISTANT"))
                .andExpect(jsonPath("$[1].text").value("Nexa answer to: I need a laptop"))
                .andExpect(jsonPath("$[2].text").value("About 900 euros"))
                .andExpect(jsonPath("$[3].role").value("ASSISTANT"));
    }

    @Test
    void theHistoryIsSentWithTheNextMessage() throws Exception {
        ask("george@example.com", "I need a laptop");
        ask("george@example.com", "About 900 euros");

        assertThat(sentHistories.get(0)).isEmpty();
        assertThat(sentHistories.get(1)).containsExactly(
                new ChatTurn(ChatRole.USER, "I need a laptop"),
                new ChatTurn(ChatRole.ASSISTANT, "Nexa answer to: I need a laptop"));
    }

    @Test
    void everyoneHasTheirOwnMemory() throws Exception {
        ask("george@example.com", "I need a laptop");
        ask("maria@example.com", "Hello");

        assertThat(sentHistories.get(1)).as("Maria must not see George's chat").isEmpty();
    }

    @Test
    void onlyTheLastTenMessagesAreSent() throws Exception {
        for (int i = 1; i <= 6; i++) {
            ask("george@example.com", "message " + i);
        }
        ask("george@example.com", "message 7");

        List<ChatTurn> seen = sentHistories.get(6);
        assertThat(seen).hasSize(AdvisorService.HISTORY_WINDOW);
        assertThat(seen.getFirst()).isEqualTo(new ChatTurn(ChatRole.USER, "message 2"));
        assertThat(seen.getLast()).isEqualTo(new ChatTurn(ChatRole.ASSISTANT, "Nexa answer to: message 6"));
    }

    @Test
    void forgettingStartsAFreshConversation() throws Exception {
        ask("george@example.com", "I need a laptop");

        mockMvc.perform(delete("/api/advisor/conversation").with(as("george@example.com")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/advisor/conversation").with(as("george@example.com")))
                .andExpect(jsonPath("$").isEmpty());

        ask("george@example.com", "Hello again");
        assertThat(sentHistories.getLast()).isEmpty();
    }

    @Test
    void aFailedModelCallStoresNothing() {
        when(advisor.reply(anyList(), anyString())).thenThrow(new IllegalStateException("model unavailable"));

        assertThatThrownBy(() -> advisorService.chat("george@example.com", "I need a laptop"))
                .isInstanceOf(com.practice.nexora.common.ServiceUnavailableException.class);

        assertThat(messages.count()).isZero();
    }
}