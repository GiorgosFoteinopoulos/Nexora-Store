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

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(properties = "nexora.advisor.timeout=300ms")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdvisorTimeoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private AdvisorClient advisor;

    @BeforeEach
    void setUp() {
        users.save(new UserAccount("george@example.com", "some-hash", Role.USER));
        when(advisor.reply(anyList(), anyString())).thenAnswer(call -> {
            Thread.sleep(3_000); // a very slow model
            return new NexaAnswer("Sorry for the wait!", List.of());
        });
    }

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    @Test
    void aSlowModelGivesUpAtTheDeadlineAndStoresNothing() throws Exception {
        long start = System.nanoTime();

        mockMvc.perform(post("/api/advisor/chat")
                        .with(jwt().jwt(token -> token.subject("george@example.com")))
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\": \"I need a laptop\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value(AdvisorService.TOO_SLOW));

        Duration waited = Duration.ofNanos(System.nanoTime() - start);
        assertThat(waited).as("the customer waited only until the deadline").isLessThan(Duration.ofSeconds(2));

        mockMvc.perform(get("/api/advisor/conversation")
                        .with(jwt().jwt(token -> token.subject("george@example.com"))))
                .andExpect(jsonPath("$").isEmpty());
    }
}