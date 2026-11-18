package com.practice.nexora.search;



import com.practice.nexora.catalog.Category;
import com.practice.nexora.common.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SemanticSearchService {
    static final int DEFAULT_LIMIT = 5;
    static final int MAX_LIMIT = 10;
    static final int MAX_QUERY_LENGTH = 300;

    private final EmbeddingClient embeddings;
    private final JdbcTemplate jdbc;

    public SemanticSearchService(EmbeddingClient embeddings, JdbcTemplate jdbc) {
        this.embeddings = embeddings;
        this.jdbc = jdbc;
    }

    public SemanticSearchResponse search(String query, Category category, BigDecimal maxPrice, int limit) {
        String question = query == null ? "" : query.strip();
        if (question.isEmpty()) {
            throw new BadRequestException("q must not be empty");
        }
        if (question.length() > MAX_QUERY_LENGTH) {
            throw new BadRequestException("q must be at most " + MAX_QUERY_LENGTH + " characters");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("limit must be between 1 and " + MAX_LIMIT);
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new BadRequestException("maxPrice must not be negative");
        }

        float[] vector = embeddings.embed(List.of(question)).getFirst();
        if (isZero(vector)) {
            return new SemanticSearchResponse(question, List.of()); // no usable words (offline toy only)
        }


        StringBuilder sql = new StringBuilder("""
                select p.slug, p.name, p.category, b.name as brand,
                       (select min(v.price_amount) from product_variants v where v.product_id = p.id) as starting_price,
                       exists (select 1 from product_variants v where v.product_id = p.id and v.stock > 0) as available,
                       e.embedding <=> ?::vector as distance
                from product_embeddings e
                join products p on p.id = e.product_id
                join brands b on b.id = p.brand_id
                where e.model = ?
                """);
        List<Object> args = new ArrayList<>(List.of(ProductEmbeddingService.toPgVector(vector), embeddings.modelName()));

        if (category != null) {
            sql.append("  and p.category = ?\n");
            args.add(category.name());
        }
        if (maxPrice != null) {
            sql.append("  and exists (select 1 from product_variants v where v.product_id = p.id and v.price_amount <= ?)\n");
            args.add(maxPrice);
        }
        sql.append("order by distance\nlimit ?");
        args.add(limit);

        List<SemanticHit> hits = jdbc.query(sql.toString(), (row, number) -> new SemanticHit(
                row.getString("slug"),
                row.getString("name"),
                Category.valueOf(row.getString("category")),
                row.getString("brand"),
                row.getBigDecimal("starting_price"),
                row.getBoolean("available"),
                similarity(row.getDouble("distance"))), args.toArray());

        return new SemanticSearchResponse(question, hits);
    }

    private static double similarity(double distance) {
        return Math.round((1 - distance) * 1000) / 1000.0;
    }

    private static boolean isZero(float[] vector) {
        for (float value : vector) {
            if (value != 0f) {
                return false;
            }
        }
        return true;
    }

}
