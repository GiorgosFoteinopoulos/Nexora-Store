package com.practice.nexora.common;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.user.Role;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ObservabilityIntegrationTest {

    private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meters;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    @Test
    void everyResponseCarriesARequestId() throws Exception {
        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.HEADER, matchesPattern(UUID_PATTERN)));
    }

    @Test
    void aSafeIdFromNginxIsKept() throws Exception {
        String fromNginx = "0123456789abcdef0123456789abcdef";

        mockMvc.perform(get("/api/brands").header(CorrelationIdFilter.HEADER, fromNginx))
                .andExpect(header().string(CorrelationIdFilter.HEADER, fromNginx));
    }

    @Test
    void anUnsafeIdIsReplaced() throws Exception {
        mockMvc.perform(get("/api/brands").header(CorrelationIdFilter.HEADER, "bad id <script>"))
                .andExpect(header().string(CorrelationIdFilter.HEADER, matchesPattern(UUID_PATTERN)));
    }

    @Test
    void metricsAreForAdminsOnly() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/metrics").with(jwt().jwt(token -> token.subject("george@example.com"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/actuator/metrics").with(jwt().jwt(token -> token.subject("admin@nexora.local"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void everyNexaAnswerIsTimed() throws Exception {
        users.save(new UserAccount("george@example.com", "some-hash", Role.USER));
        double before = answeredCount();
        mockMvc.perform(post("/api/advisor/chat")
                        .with(jwt().jwt(token -> token.subject("george@example.com")))
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\": \"Hi Nexa\"}"))
                .andExpect(status().isOk());

        assertThat(answeredCount()).isEqualTo(before + 1);
    }

    private double answeredCount() {
        Timer timer = meters.find("nexa.chat").tag("outcome", "ok").timer();
        return timer == null ? 0 : timer.count();
    }
}