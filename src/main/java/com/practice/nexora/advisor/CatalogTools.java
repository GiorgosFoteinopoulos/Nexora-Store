package com.practice.nexora.advisor;


import com.practice.nexora.catalog.*;
import com.practice.nexora.common.BadRequestException;
import com.practice.nexora.common.Slug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class CatalogTools {

    private static final Logger log = LoggerFactory.getLogger(CatalogTools.class);

    static final int MAX_RESULTS = 5;

    private final ProductService products;
    private final ComparisonService comparisons;

    public CatalogTools(ProductService products, ComparisonService comparisons) {
        this.products = products;
        this.comparisons = comparisons;
    }

    @Tool(description = """
            Search the Nexora Store catalog. Returns at most 5 matching products with slug, name, category, brand, starting price
            in euros, availability and a short spec summary.
            All filters are optional and can be combined. Uset this before recommending anything.""")
    public List<ProductSummaryResponse> searchProducts(
            @ToolParam(description = "Category: phone, laptop, tablet, wearable or accessory", required = false)
            String category,
            @ToolParam(description = "Brand: nexora, voltix or lumio", required = false)
            String brand,
            @ToolParam(description = "Minimum starting price in euros", required = false)
            BigDecimal minPrice,
            @ToolParam(description = "Maximum price in euros, usually the customer's budget", required = false)
            BigDecimal maxPrice,
            @ToolParam(description = "ONE keyword to find in the name or description, for example: gaming, students, "
            + "travel, battery, kids, outdoor. Not a whole sentence.", required = false)
            String text) {
        log.info("Nexa tool: searchProducts(category={}, brand={}, minPrice={}. maxPrice={}, text={})",
                category, brand, minPrice, maxPrice, text);
        requireNotNegative(minPrice, "minPrice");
        requireNotNegative(maxPrice, "maxPrice");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("minPrice must not be greater than maxPrice");
        }

        ProductFilter filter = new ProductFilter(parseCategory(category), normalizeBrand(brand), minPrice, maxPrice, text);
        return products.listProducts(filter, PageRequest.of(0, MAX_RESULTS)).content();
    }

    @Tool(description = """
            Get the full details of ONE product by its slug: description, specifications and every variant
            (colour, storage, price in euros, stock). Use it when the customer asks about a specific product.""")
    public ProductResponse getProductDetails(
            @ToolParam(description = "The product slug, exactly as returned by searchProducts, e.g. voltix-air-13")
            String slug) {
        log.info("Nexa tool: getProductDetails(slug={})", slug);
        return products.getProduct(normalizeSlug(slug));
    }

    @Tool(description = """
            Compare 2 to 4 products of the SAME category side by side (specs, prices, availability).
            Use it when the customer hesitates between products.""")
    public ComparisonResponse compareProducts(
            @ToolParam(description = "2 to 4 product slugs of the same category, as returned by searchProducts")
            List<String> slugs) {
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
