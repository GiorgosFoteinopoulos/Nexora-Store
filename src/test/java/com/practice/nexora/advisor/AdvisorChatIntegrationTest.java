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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class AdvisorChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private AdvisorClient advisor;

    @BeforeEach
    void createGeorge() {
        users.save(new UserAccount("george@example.com", "some-hash", Role.USER));
    }

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    private void nexaWillAnswer(String reply, List<String> slugs) {
        when(advisor.reply(anyList(), anyString())).thenReturn(new NexaAnswer(reply, slugs));
    }

    private ResultActions ask(String message) throws Exception {
        return mockMvc.perform(post("/api/advisor/chat")
                .with(jwt().jwt(token -> token.subject("george@example.com")))
                .contentType(APPLICATION_JSON)
                .content("{\"message\": \"" + message + "\"}"));
    }

    @Test
    void recommendedProductsComeBackAsCardsWithLiveData() throws Exception {
        nexaWillAnswer("The Voltix Air 13 is light and affordable.", List.of("voltix-air-13"));

        ask("A light laptop for university?")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("The Voltix Air 13 is light and affordable."))
                .andExpect(jsonPath("$.products", hasSize(1)))
                .andExpect(jsonPath("$.products[0].slug").value("voltix-air-13"))
                .andExpect(jsonPath("$.products[0].name").value("Voltix Air 13"));
    }

    @Test
    void inventedProductsAreDropped() throws Exception {
        nexaWillAnswer("Try these two.", List.of("voltix-air-13", "nexora-hologram-x"));

        ask("Something futuristic?")
                .andExpect(jsonPath("$.products", hasSize(1)))
                .andExpect(jsonPath("$.products[0].slug").value("voltix-air-13"));
    }

    @Test
    void duplicatesAreRemovedAndAtMostThreeCardsAreShown() throws Exception {
        nexaWillAnswer("Many options!", List.of(
                "voltix-air-13", " VOLTIX-AIR-13 ", "nexora-book-14", "lumio-book-14", "voltix-air-15"));

        ask("Show me laptops")
                .andExpect(jsonPath("$.products", hasSize(3)))
                .andExpect(jsonPath("$.products[0].slug").value("voltix-air-13"))
                .andExpect(jsonPath("$.products[1].slug").value("nexora-book-14"))
                .andExpect(jsonPath("$.products[2].slug").value("lumio-book-14"));
    }

    @Test
    void aFollowUpQuestionComesWithoutCards() throws Exception {
        nexaWillAnswer("What is your budget, and what will you use it for?", List.of());

        ask("I need a laptop")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty());
    }
}