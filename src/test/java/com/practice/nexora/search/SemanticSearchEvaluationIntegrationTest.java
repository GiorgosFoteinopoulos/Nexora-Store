package com.practice.nexora.search;

import com.practice.nexora.TestcontainersConfiguration;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class SemanticSearchEvaluationIntegrationTest {

    private static final String RUGGED_QUESTION = "rugged outdoor phone for hiking and camping";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SemanticSearchEvaluator evaluator;

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
    void theGoldenSetIsBigEnoughAndHasNoDuplicates() {
        List<GoldenSet.Question> questions = SemanticSearchEvaluator.loadGoldenSet().questions();

        assertThat(questions).hasSizeGreaterThanOrEqualTo(15);
        assertThat(questions).extracting(GoldenSet.Question::question).doesNotHaveDuplicates();
        assertThat(questions).allSatisfy(question -> assertThat(question.expected()).isNotEmpty());
    }

    @Test
    void everyExpectedProductExistsInTheCatalog() {
        List<String> slugs = jdbc.queryForList("select slug from products", String.class);
        List<String> expected = SemanticSearchEvaluator.loadGoldenSet().questions().stream()
                .flatMap(question -> question.expected().stream())
                .toList();

        assertThat(slugs).containsAll(expected);
    }

    @Test
    void theScoresAreCalculatedCorrectly() {
        EvaluationReport report = evaluator.evaluate(List.of(
                new GoldenSet.Question(RUGGED_QUESTION, List.of("voltix-rugged-r2")),
                new GoldenSet.Question(RUGGED_QUESTION, List.of("no-such-product"))));

        assertThat(report.results()).extracting(EvaluationReport.QuestionResult::rank).containsExactly(1, 0);
        assertThat(report.hitAt1()).isEqualTo(0.5);
        assertThat(report.hitAt3()).isEqualTo(0.5);
        assertThat(report.meanReciprocalRank()).isEqualTo(0.5);
    }

    @Test
    void theWholeGoldenSetRunsOffline() {
        EvaluationReport report = evaluator.evaluate();

        assertThat(report.model()).isEqualTo(OfflineEmbeddingClient.MODEL);
        assertThat(report.questions()).isEqualTo(report.results().size());
        assertThat(report.results())
                .allSatisfy(result -> assertThat(result.rank()).isBetween(0, SemanticSearchEvaluator.TOP_K));
        assertThat(report.hitAt1()).isBetween(0.0, report.hitAt3());
        assertThat(report.meanReciprocalRank()).isBetween(0.0, 1.0);
    }

    @Test
    void onlyAnAdminMayRunTheEvaluation() throws Exception {
        mockMvc.perform(post("/api/admin/embeddings/evaluate")
                        .with(jwt().jwt(token -> token.subject("george@example.com"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anAdminGetsTheReport() throws Exception {
        mockMvc.perform(post("/api/admin/embeddings/evaluate")
                        .with(jwt().jwt(token -> token.subject("admin@nexora.local"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("offline-word-hashing"))
                .andExpect(jsonPath("$.hitAt3").isNumber())
                .andExpect(jsonPath("$.results[0].question").isNotEmpty());
    }
}