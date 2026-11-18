package com.practice.nexora.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/embeddings")
@Tag(name = "Admin: semantic search")
class EmbeddingAdminController {

    private final ProductEmbeddingService embeddings;

    EmbeddingAdminController(ProductEmbeddingService embeddings) {
        this.embeddings = embeddings;
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Embed new or changed products (idempotent: unchanged products are skipped)")
    EmbeddingRun refresh() {
        return embeddings.refresh();
    }
}
