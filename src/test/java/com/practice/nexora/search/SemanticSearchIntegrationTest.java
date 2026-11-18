package com.practice.nexora.search;

import com.jayway.jsonpath.JsonPath;
import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class SemanticSearchIntegrationTest {

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

    @Test
    void theClosestProductComesFirst_noTokenNeeded() throws Exception {
        mockMvc.perform(get("/api/products/semantic").param("q", "rugged outdoor phone for hiking and camping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits[0].slug").value("voltix-rugged-r2"));
    }

    @Test
    void resultsAreSortedFromMostToLeastSimilar() throws Exception {
        String json = mockMvc.perform(get("/api/products/semantic").param("q", "light laptop for students").param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Double> similarities = JsonPath.read(json, "$.hits[*].similarity");
        assertThat(similarities).hasSize(10).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void theCategoryFilterIsAHardRule() throws Exception {
        mockMvc.perform(get("/api/products/semantic").param("q", "light for travel").param("category", "LAPTOP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits[*].category", everyItem(is("LAPTOP"))));
    }

    @Test
    void theMaxPriceFilterIsAHardRule() throws Exception {
        mockMvc.perform(get("/api/products/semantic").param("q", "laptop for work").param("maxPrice", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits[*].startingPrice", everyItem(lessThanOrEqualTo(500.0))));
    }

    @Test
    void theLimitIsRespectedAndCapped() throws Exception {
        mockMvc.perform(get("/api/products/semantic").param("q", "phone").param("limit", "3"))
                .andExpect(jsonPath("$.hits", hasSize(3)));
        mockMvc.perform(get("/api/products/semantic").param("q", "phone").param("limit", "50"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void anEmptyQuestionIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/products/semantic").param("q", "   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void theHnswIndexExists() {
        Integer indexes = jdbc.queryForObject(
                "select count(*) from pg_indexes where indexname = 'ix_product_embeddings_hnsw'", Integer.class);
        assertThat(indexes).isEqualTo(1);
    }
}
