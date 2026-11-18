package com.practice.nexora.search;

import com.practice.nexora.catalog.Category;

import java.math.BigDecimal;

public record SemanticHit(
        String slug,
        String name,
        Category category,
        String brand,
        BigDecimal startingPrice,
        boolean available,
        double similarity)
{
}
