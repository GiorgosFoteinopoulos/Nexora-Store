package com.practice.nexora.user;

import com.practice.nexora.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginIntegrationTest {

    private static final String GEORGE = """
            {"email": "george@example.com", "password": "correct-horse"} """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountRepository accounts;

    @AfterEach
    void cleanUp() {
        accounts.deleteAll();
    }

    private String registerAndLogin() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(GEORGE))
                .andExpect(status().isCreated());
        String loginAnswer = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(GEORGE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(loginAnswer, "$.accessToken");
    }

    @Test
    void theTokenOpensProtectedEndpoints() throws Exception {
        String token = registerAndLogin();

        mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("george@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void withoutATokenTheDoorStaysClosed() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void aChangedTokenIsRefused() throws Exception {
        String token = registerAndLogin();
        String tampered = token.substring(0, token.length() - 2) + "xx";

        mvc.perform(get("/api/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }
}
