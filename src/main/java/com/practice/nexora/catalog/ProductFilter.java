package com.practice.nexora.catalog;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.Parameter;

public record ProductFilter(
        @Parameter(description = "Only products of this category") Category category,
        @Parameter(description = "Only products of this brand (its slug, e.g. nexora)") String brand,
        @Parameter(description = "Only products with a variant priced at least this, in euros") BigDecimal minPrice,
        @Parameter(description = "Only products with a variant priced at most this, in euros") BigDecimal maxPrice,
        @Parameter(description = "Text to find in the name or description, ignoring case") String q) {

    public ProductFilter {
        brand = blankToNull(brand);
        q = blankToNull(q);
    }

    public static ProductFilter none() {
        return new ProductFilter(null, null, null, null, null);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}


