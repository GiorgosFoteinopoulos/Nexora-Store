package com.practice.nexora.advisor;


import com.practice.nexora.catalog.*;
import com.practice.nexora.common.BadRequestException;
import com.practice.nexora.common.Slug;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class CatalogTools {

    static final int MAX_RESULTS = 5;

    private final ProductService products;
    private final ComparisonService comparisons;

    public CatalogTools(ProductService products, ComparisonService comparisons) {
        this.products = products;
        this.comparisons = comparisons;
    }

    public List<ProductSummaryResponse> searchProducts(String category, String brand,
                                                       BigDecimal minPrice, BigDecimal maxPrice, String text) {
        requireNotNegative(minPrice, "minPrice");
        requireNotNegative(maxPrice, "maxPrice");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("minPrice must not be greater than maxPrice");
        }

        ProductFilter filter = new ProductFilter(parseCategory(category), normalizeBrand(brand), minPrice, maxPrice, text);
        return products.listProducts(filter, PageRequest.of(0, MAX_RESULTS)).content();
    }

    public ProductResponse getProductDetails(String slug) {
        return products.getProduct(normalizeSlug(slug));
    }

    public ComparisonResponse compareProducts(List<String> slugs) {
        if (slugs == null) {
            throw new BadRequestException("Give 2 to 4 product slugs to compare");
        }
        return comparisons.compare(slugs.stream().map(CatalogTools::normalizeSlug).toList());
    }

    static Category parseCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String wanted = singular(raw.strip());
        return Arrays.stream(Category.values())
                .filter(category -> singular(category.name()).equals(wanted)
                        || singular(category.getDisplayName()).equals(wanted))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Unknown category '%s'. Use one of: %s".formatted(raw, allowedCategories())));
    }

    private static String singular(String word) {
        String lower = word.toLowerCase(Locale.ROOT);
        if (lower.endsWith("ies")) {
            return lower.substring(0, lower.length() - 3) + "y";
        }
        if (lower.endsWith("s")) {
            return lower.substring(0, lower.length() - 1);
        }
        return lower;
    }

    private static String allowedCategories() {
        return Arrays.stream(Category.values())
                .map(category -> category.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", "));
    }
    private static String normalizeBrand(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Slug.from(raw).value();
        } catch (IllegalArgumentException unusable) {
            return raw.strip();
        }
    }

        private static String normalizeSlug(String raw) {
            return raw == null ? "" : raw.strip().toLowerCase(Locale.ROOT);
        }

        private static void requireNotNegative(BigDecimal value, String name) {
        if (value != null && value.signum() < 0) {
            throw new BadRequestException(name + " must not be negative");
        }
        }

}
