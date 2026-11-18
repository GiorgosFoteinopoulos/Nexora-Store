package com.practice.nexora.advisor;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.catalog.Category;
import com.practice.nexora.search.ProductEmbeddingService;
import com.practice.nexora.search.SemanticHit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class CatalogToolsSemanticTest {

    @Autowired
    private CatalogTools tools;

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
    void nexaCanFindProductsByANeedInWords() {
        List<SemanticHit> hits = tools.findProductsByMeaning("rugged outdoor phone for hiking and camping", null, null);

        assertThat(hits.getFirst().slug()).isEqualTo("voltix-rugged-r2");
    }

    @Test
    void theCategoryIsForgivingHereToo() {
        List<SemanticHit> hits = tools.findProductsByMeaning("light for travel", "Phones", null);

        assertThat(hits).isNotEmpty().allMatch(hit -> hit.category() == Category.PHONE);
    }
}