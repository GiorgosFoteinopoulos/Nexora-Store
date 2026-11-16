package com.practice.nexora.catalog;

import com.practice.nexora.common.BadRequestException;
import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.common.Slug;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ComparisonService {

    private final ProductRepository products;

    public ComparisonService(ProductRepository products) {
        this.products = products;
    }

    public ComparisonResponse compare(List<String> rawSlugs) {

        if (rawSlugs.size() > Comparison.MAX_PRODUCTS) {
            throw new BadRequestException("A comparison needs %d to %d products, got %d"
                    .formatted(Comparison.MIN_PRODUCTS, Comparison.MAX_PRODUCTS, rawSlugs.size()));
        }

        List<Slug> validSlugs = rawSlugs.stream()
                .map(ComparisonService::parseSlug)
                .flatMap(Optional::stream)
                .toList();
        Map<Slug, Product> bySlug = products.findBySlugIn(validSlugs).stream()
                .collect(Collectors.toMap(Product::getSlug, Function.identity()));


        List<String> missing = rawSlugs.stream()
                .filter(raw -> parseSlug(raw).map(slug -> !bySlug.containsKey(slug)).orElse(true))
                .toList();
        if (!missing.isEmpty()) {
            throw new NotFoundException("Products not found: " + String.join(", ", missing));
        }


        List<Product> inRequestedOrder = rawSlugs.stream()
                .map(raw -> bySlug.get(parseSlug(raw).orElseThrow()))
                .toList();


        try {
            return ComparisonResponse.from(new Comparison(inRequestedOrder));
        } catch (IllegalArgumentException brokenRule) {
            throw new BadRequestException(brokenRule.getMessage());
        }
    }

  
    private static Optional<Slug> parseSlug(String raw) {
        try {
            return Optional.of(new Slug(raw));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }
}