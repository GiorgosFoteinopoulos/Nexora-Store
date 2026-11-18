package com.practice.nexora.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "nexora.advisor", name = "provider", havingValue = "openai")
class EmbeddingStartupRefresh implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingStartupRefresh.class);

    private final ProductEmbeddingService embeddings;

    EmbeddingStartupRefresh(ProductEmbeddingService embeddings) {
        this.embeddings = embeddings;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            EmbeddingRun run = embeddings.refresh();
            log.info("Product embeddings: {} embedded, {} already up to date", run.embedded(), run.alreadyUpToDate());
        } catch (RuntimeException failed) {
            log.warn("Could not refresh product embeddings at startup (semantic search may be stale): {}",
                    failed.getMessage());
        }
    }
}
