package com.practice.nexora.catalog;


import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.common.Slug;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public ProductResponse getProduct(String slug) {
        return parse(slug)
                .flatMap(products::findBySlug)
                .map(ProductResponse::from)
                .orElseThrow(() -> new NotFoundException("No product with slug '%s'".formatted(slug)));
    }

    private static Optional<Slug> parse(String value) {
        try {
            return Optional.of(new Slug(value));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }
}
