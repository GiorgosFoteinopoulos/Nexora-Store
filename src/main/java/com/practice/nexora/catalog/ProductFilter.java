package com.practice.nexora.catalog;

import java.math.BigDecimal;

public record ProductFilter(Category category, String brand, BigDecimal minPrice, BigDecimal maxPrice, String q) {

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


