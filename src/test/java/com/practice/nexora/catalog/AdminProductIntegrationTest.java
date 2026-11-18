package com.practice.nexora.catalog;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.search.ProductEmbeddingService;
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

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class AdminProductIntegrationTest {

    private static final String SLUG = "voltix-air-13";
    private static final String NEW_TEXT = "Compact, very light laptop, now perfect for long flights and trips.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductEmbeddingService embeddings;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void embedTheCatalog() {
        embeddings.refresh();
    }

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    private static RequestPostProcessor asAdmin() {
        return jwt().jwt(token -> token.subject("admin@nexora.local"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private String storedHash() {
        return jdbc.queryForObject("""
                select e.content_hash from product_embeddings e
                join products p on p.id = e.product_id where p.slug = ?
                """, String.class, SLUG);
    }

    @Test
    void changingADescriptionRenewsItsVectorAfterCommit() throws Exception {
        String before = storedHash();

        mockMvc.perform(patch("/api/admin/products/{slug}", SLUG).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\": \"" + NEW_TEXT + "\"}"))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("select description from products where slug = ?", String.class, SLUG))
                .isEqualTo(NEW_TEXT);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(storedHash()).isNotEqualTo(before));
    }

    @Test
    void onlyAnAdminMayChangeADescription() throws Exception {
        mockMvc.perform(patch("/api/admin/products/{slug}", SLUG)
                        .with(jwt().jwt(token -> token.subject("george@example.com")))
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\": \"" + NEW_TEXT + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void aBlankDescriptionIsABadRequest() throws Exception {
        mockMvc.perform(patch("/api/admin/products/{slug}", SLUG).with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnknownProductIsNotFound() throws Exception {
        mockMvc.perform(patch("/api/admin/products/{slug}", "no-such-product").with(asAdmin())
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\": \"" + NEW_TEXT + "\"}"))
                .andExpect(status().isNotFound());
    }
}