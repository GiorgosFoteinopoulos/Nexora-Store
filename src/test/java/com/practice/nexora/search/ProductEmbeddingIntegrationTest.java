package com.practice.nexora.search;


import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class ProductEmbeddingIntegrationTest {

    private static final int PRODUCTS = 46;

    @Autowired
    private ProductEmbeddingService embeddings;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    @Test
    void theFirstRefreshEmbedsEveryProduct() {
        EmbeddingRun run = embeddings.refresh();

        assertThat(run).isEqualTo(new EmbeddingRun(PRODUCTS, 0));
        assertThat(jdbc.queryForObject("select count(*) from product_embeddings", Integer.class)).isEqualTo(PRODUCTS);
        assertThat(jdbc.queryForObject("select min(vector_dims(embedding)) from product_embeddings", Integer.class))
                .isEqualTo(EmbeddingClient.DIMENSIONS);
        assertThat(jdbc.queryForObject("select distinct model from product_embeddings", String.class))
                .isEqualTo(OfflineEmbeddingClient.MODEL);
    }

    @Test
    void aSecondRefreshEmbedsNothing_idempotent() {
        embeddings.refresh();

        assertThat(embeddings.refresh()).isEqualTo(new EmbeddingRun(0, PRODUCTS));
    }

    @Test
    void aChangedDescriptionIsEmbeddedAgain() {
        embeddings.refresh();
        jdbc.update("update products set description = 'Now with an even longer battery life for long trips.' "
        + "where slug = 'voltix-air-13'");

        assertThat(embeddings.refresh()).isEqualTo(new EmbeddingRun(1, PRODUCTS - 1));
    }

    @Test
    void onlyAdminMayTriggerARefresh() throws Exception {
        mockMvc.perform(post("/api/admin/embeddings/refresh")
                .with(jwt().jwt(token -> token.subject("george@example.com"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/embeddings/refresh")
                .with(jwt().jwt(token -> token.subject("admin@nexoral.local"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embedded").value(PRODUCTS))
                .andExpect(jsonPath("$.alreadyUpToDate").value(0));
    }
}
