package com.practice.nexora.catalog;


import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.common.Slug;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.practice.nexora.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    static final Sort CATALOG_ORDER = Sort.by("name", "id");
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

    public PageResponse<ProductSummaryResponse> listProducts(ProductFilter filter, Pageable requested) {
        Pageable pageable = PageRequest.of(requested.getPageNumber(), requested.getPageSize(), CATALOG_ORDER);
        Specification<Product> conditions = ProductSpecifications.matching(filter);
        Page<Product> page = products.findAll(conditions, pageable);
        return PageResponse.from(page.map(ProductSummaryResponse::from));
    }
}
