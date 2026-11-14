package com.practice.nexora.user;

import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RegistrationIntegrationTest {

    private static final String GEORGE = """
            {"email": "George@Example.com", "password": "correct-horse"}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountRepository accounts;

    @AfterEach
    void cleanUp() {
        accounts.deleteAll();
    }

    @Test
    void registeringIsPublicAndStoresOnlyABcryptHash() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(GEORGE))
                .andExpect(status().isCreated()); // no login needed: the security rule from part 11

        UserAccount stored = accounts.findByEmail("george@example.com").orElseThrow();
        assertThat(stored.getPasswordHash()).startsWith("$2a$").doesNotContain("correct-horse");
    }

    @Test
    void theSameEmailCannotRegisterTwice() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(GEORGE))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(GEORGE))
                .andExpect(status().isConflict());
    }
}