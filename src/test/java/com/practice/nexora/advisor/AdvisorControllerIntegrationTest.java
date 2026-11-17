package com.practice.nexora.advisor;

import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdvisorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdvisorClient advisor;

    @Test
    void testsNeverTalkToTheRealModel() {
        assertThat(advisor).isInstanceOf(OfflineAdvisorClient.class);
    }

    @Test
    void aLoggedInUserGetsAReply() throws Exception {
        mockMvc.perform(post("/api/advisor/chat")
                        .with(jwt().jwt(token -> token.subject("george@example.com")))
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\": \"Hi Nexa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value(OfflineAdvisorClient.REPLY));
    }

    @Test
    void anonymousUsersCannotChat() throws Exception {
        mockMvc.perform(post("/api/advisor/chat")
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\": \"Hi Nexa\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anEmptyMessageIsABadRequest() throws Exception {
        mockMvc.perform(post("/api/advisor/chat")
                .with(jwt().jwt(token -> token.subject("george@example.com")))
                .contentType(APPLICATION_JSON)
                .content("{\"message\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aTooLongMessageIsABadRequest() throws Exception {
        String essay = "a".repeat(1001);
        mockMvc.perform(post("/api/advisor/chat")
                .with(jwt().jwt(token -> token.subject("george@example.com")))
                .contentType(APPLICATION_JSON)
                .content("{\"message\": \"" + essay + "\"}"))
                .andExpect(status().isBadRequest());
    }
}
