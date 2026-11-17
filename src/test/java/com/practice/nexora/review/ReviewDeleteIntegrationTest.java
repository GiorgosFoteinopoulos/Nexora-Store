package com.practice.nexora.review;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

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
@Sql("/db/dev/R__dev_catalog.sql")
class ReviewDeleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    private String slug;

    @BeforeEach
    void pickAProductSlugFromTheCatalog() throws Exception {
        String json = mockMvc.perform(get("/api/products").param("size", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        slug = JsonPath.read(json, "$.content[0].slug");
    }

    @AfterEach
    void emptyDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    private static RequestPostProcessor as(String email) {
        return jwt().jwt(token -> token.subject(email));
    }

    private static RequestPostProcessor asAdmin(String email) {
        return jwt().jwt(token -> token.subject(email))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private long georgeWritesAReview() throws Exception {
        users.save(new UserAccount("george@example.com", "some-hash", Role.USER));
        String json = mockMvc.perform(post("/api/products/{slug}/reviews", slug)
                .with(as("george@example.com"))
                .contentType(APPLICATION_JSON)
                .content("{\"rating\":5}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(json, "$.id");
        return id.longValue();
    }

    private void expectReviewCount(int expected) throws Exception {
        mockMvc.perform(get("/api/products/{slug}/reviews", slug))
                .andExpect(jsonPath("$.reviewCount").value(expected));
    }

    @Test
    void theAuthorDeletesTheirOwnReview() throws Exception {
        long id = georgeWritesAReview();

        mockMvc.perform(delete("/api/reviews/{id}", id).with(as("george@example.com")))
                .andExpect(status().isNoContent());

        expectReviewCount(0);
    }


    @Test
    void anAdminCanDeleteAnyReview() throws Exception {
        long id = georgeWritesAReview();

        mockMvc.perform(delete("/api/reviews/{id}", id).with(asAdmin("admin@nexora.local")))
                .andExpect(status().isNoContent());

        expectReviewCount(0);
    }

    @Test
    void withoutATokenItIsUnauthorized() throws Exception {
        long id = georgeWritesAReview();

        mockMvc.perform(delete("/api/reviews/{id}", id))
                .andExpect(status().isUnauthorized());

        expectReviewCount(1);
    }

    @Test
    void anUnknownReviewIsNotFound() throws Exception {
        mockMvc.perform(delete("/api/reviews/{id}", 999_999).with(as("george@example.com")))
                .andExpect(status().isNotFound());
    }
}
