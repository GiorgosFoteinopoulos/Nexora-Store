package com.practice.nexora.search;

import com.practice.nexora.catalog.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@Tag(name = "Semantic search")
class SemanticSearchController {

    private final SemanticSearchService search;

    public SemanticSearchController(SemanticSearchService search) {
        this.search = search;
    }

    @GetMapping("/api/products/semantic")
    @Operation(summary = "Find products by MEANING (public), optionally only one category and/or up to a max price")
    SemanticSearchResponse semantic(@RequestParam String q,
                                    @RequestParam(required = false) Category category,
                                    @RequestParam(required = false)BigDecimal maxPrice,
                                    @RequestParam(defaultValue = "" + SemanticSearchService.DEFAULT_LIMIT) int limit) {
        return search.search(q, category, maxPrice, limit);
    }
}
