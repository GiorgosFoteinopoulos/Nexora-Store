package com.practice.nexora.search;


import com.practice.nexora.catalog.Product;
import com.practice.nexora.catalog.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class ProductEmbeddingService {

    static final int BATCH_SIZE = 20;

    private final ProductRepository products;
    private final EmbeddingClient embeddings;
    private JdbcTemplate jdbc;
    private final TransactionTemplate readOnly;

    public ProductEmbeddingService(ProductRepository products, EmbeddingClient embeddings,
                                   JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.products = products;
        this.embeddings = embeddings;
        this.jdbc = jdbc;
        this.readOnly = new TransactionTemplate(transactionManager);
        this.readOnly.setReadOnly(true);
    }

    public EmbeddingRun refresh() {
        Pending pending = readOnly.execute(status -> findPending());


        for (List<PendingText> batch : batches(pending.texts())) {
            List<float[]> vectors = embeddings.embed(batch.stream().map(PendingText::text).toList());
            for (int i = 0; i < batch.size(); i++) {
                store(batch.get(i), vectors.get(i));
            }
        }
        return new EmbeddingRun(pending.texts().size(), pending.total() - pending.texts().size());
    }

    static String textOf(Product product) {
        return "%s. %s by %s. %s".formatted(
                product.getName(),
                product.getCategory().getDisplayName(),
                product.getBrand().getName(),
                product.getDescription()
        );
    }

    static String fingerprint(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash); // 64 hex characters
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is always available in Java", impossible);
        }
    }

    private record PendingText(long productId, String text, String hash) {

    }

    private record Pending(List<PendingText> texts, int total) {

    }

    private record Stored(String hash, String model) {

    }

    private Pending findPending() {
        Map<Long, Stored> stored = new HashMap<>();
        jdbc.query("select product_id, content_hash, model from product_embeddings",
                (RowCallbackHandler) row -> stored.put(row.getLong("product_id"),
                        new Stored(row.getString("content_hash"), row.getString("model"))));

        List<Product> all = products.findAll(Sort.by("id"));
        List<PendingText> pending = new ArrayList<>();
        for (Product product : all) {
            String text = textOf(product);
            String hash = fingerprint(text);
            Stored current = stored.get(product.getId());
            boolean upToDate = current != null
                    && current.hash().equals(hash)
                    && current.model().equals(embeddings.modelName());
            if (!upToDate) {
                pending.add(new PendingText(product.getId(), text, hash));
            }
        }
        return new Pending(pending, all.size());
    }

    private void store(PendingText item, float[] vector) {
        jdbc.update("""
                insert into product_embeddings (product_id, embedding, content_hash, model, embedded_at)
                values (?, ?::vector, ?, ?, now())
                on conflict (product_id) do update
                   set embedding    = excluded.embedding,
                       content_hash = excluded.content_hash,
                       model        = excluded.model,
                       embedded_at  = excluded.embedded_at
                """, item.productId(), toPgVector(vector), item.hash(), embeddings.modelName());
    }

    static String toPgVector(float[] vector) {
        StringBuilder text = new StringBuilder(vector.length * 8).append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                text.append(',');
            }
            text.append(vector[i]);
        }
        return text.append(']').toString();
    }

    private static <T> List<List<T>> batches(List<T> items) {
        int count = (items.size() + BATCH_SIZE - 1) / BATCH_SIZE;
        return IntStream.range(0, count)
                .mapToObj(i -> items.subList(i * BATCH_SIZE, Math.min(items.size(), (i + 1) * BATCH_SIZE)))
                .toList();
    }
}
