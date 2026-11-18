package com.practice.nexora.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;


@Service
public class SemanticSearchEvaluator {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchEvaluator.class);

    static final String GOLDEN_SET = "eval/semantic-golden-set.json";
    static final int TOP_K = 5;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final SemanticSearchService search;
    private final EmbeddingClient embeddings;

    public SemanticSearchEvaluator(SemanticSearchService search, EmbeddingClient embeddings) {
        this.search = search;
        this.embeddings = embeddings;
    }

    
    public EvaluationReport evaluate() {
        EvaluationReport report = evaluate(loadGoldenSet().questions());
        log.info("Semantic search evaluation ({}): hit@1 {}, hit@3 {}, MRR {} over {} questions",
                report.model(), report.hitAt1(), report.hitAt3(), report.meanReciprocalRank(), report.questions());
        return report;
    }

    EvaluationReport evaluate(List<GoldenSet.Question> questions) {
        List<EvaluationReport.QuestionResult> results = questions.stream().map(this::mark).toList();
        int total = results.size();

        long first = results.stream().filter(result -> result.rank() == 1).count();
        long topThree = results.stream().filter(result -> result.rank() >= 1 && result.rank() <= 3).count();
        double reciprocalSum = results.stream()
                .mapToDouble(result -> result.rank() == 0 ? 0 : 1.0 / result.rank())
                .sum();

        return new EvaluationReport(
                embeddings.modelName(),
                total,
                share(first, total),
                share(topThree, total),
                total == 0 ? 0 : round(reciprocalSum / total),
                results);
    }

    private EvaluationReport.QuestionResult mark(GoldenSet.Question question) {
        List<String> found = search.search(question.question(), null, null, TOP_K).hits().stream()
                .map(SemanticHit::slug)
                .toList();
        int rank = 0;
        for (int position = 0; position < found.size(); position++) {
            if (question.expected().contains(found.get(position))) {
                rank = position + 1;
                break;
            }
        }
        return new EvaluationReport.QuestionResult(question.question(), question.expected(), found, rank);
    }

    static GoldenSet loadGoldenSet() {
        try (InputStream in = new ClassPathResource(GOLDEN_SET).getInputStream()) {
            return JSON.readValue(in, GoldenSet.class);
        } catch (IOException unreadable) {
            throw new UncheckedIOException("Cannot read " + GOLDEN_SET, unreadable);
        }
    }

    private static double share(long part, int total) {
        return total == 0 ? 0 : round((double) part / total);
    }

    private static double round(double value) {
        return Math.round(value * 1000) / 1000.0;
    }
}