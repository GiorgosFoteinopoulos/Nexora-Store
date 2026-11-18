package com.practice.nexora.search;


import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.practice.nexora.catalog.ProductTextChanged;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
class ReembedAfterProductChange {

    private static final Logger log = LoggerFactory.getLogger(ReembedAfterProductChange.class);

    private final ProductEmbeddingService embeddings;
    private final ExecutorService worker = Executors.newVirtualThreadPerTaskExecutor();

    ReembedAfterProductChange(ProductEmbeddingService embeddings) {
        this.embeddings = embeddings;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onProductTextChanged(ProductTextChanged event) {
        worker.submit(() -> {
            try {
                EmbeddingRun run = embeddings.refresh();
                log.info("Re-embedded after change of product {}: {} embedded, {} up to date",
                        event.productId(), run.embedded(), run.alreadyUpToDate());
            } catch (RuntimeException failed) {

                log.warn("Re-embedding after change of product {} failed: {}", event.productId(), failed.getMessage());
            }
        });
    }

    @PreDestroy
    void stop() {
        worker.close();
    }
}