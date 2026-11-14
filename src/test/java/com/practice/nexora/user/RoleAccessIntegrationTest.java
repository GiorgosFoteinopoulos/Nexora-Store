package com.practice.nexora.user;

import com.jayway.jsonpath.JsonPath;
import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RoleAccessIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createOneUserAndOneAdmin() {
        accounts.save(new UserAccount("george@example.com", passwordEncoder.encode("correct-horse"), Role.USER));
        accounts.save(new UserAccount("admin@nexora.local", passwordEncoder.encode("admin-password"), Role.ADMIN));
    }

    @AfterEach
    void cleanUp() {
        accounts.deleteAll();
    }

    private String tokenFor(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
        String answer = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(answer, "$.accessToken");
    }

    @Test
    void anAdminCanListTheAccounts() throws Exception {
        String token = tokenFor("admin@nexora.local", "admin-password");

        mvc.perform(get("/api/admin/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("admin@nexora.local", "george@example.com")));
    }

    @Test
    void aNormalUserIsForbidden() throws Exception {
        String token = tokenFor("george@example.com", "correct-horse");

        mvc.perform(get("/api/admin/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void withoutATokenItIsUnauthorized() throws Exception {
        mvc.perform(get("/api/admin/accounts"))
                .andExpect(status().isUnauthorized());
    }

}
