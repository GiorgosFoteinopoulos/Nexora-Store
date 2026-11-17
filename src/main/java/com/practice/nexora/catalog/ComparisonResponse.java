package com.practice.nexora.catalog;

import java.util.List;
import java.util.Map;

public record ComparisonResponse(
        String category,
        List<String> products,
        Map<String, List<String>> table,
        String cheapest
) {

    static ComparisonResponse from(Comparison comparison) {
        return new ComparisonResponse(
                comparison.getCategory().getDisplayName(),
                comparison.getProducts().stream().map(product -> product.getSlug().value()).toList(),
                comparison.table(),
                comparison.cheapest().map(product -> product.getSlug().value()).orElse(null)
        );
    }
}
