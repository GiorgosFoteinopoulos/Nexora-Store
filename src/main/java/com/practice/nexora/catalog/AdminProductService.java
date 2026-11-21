package com.practice.nexora.catalog;

import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.common.Slug;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AdminProductService {

    private final ProductRepository products;
    private final ApplicationEventPublisher events;

    public AdminProductService(ProductRepository products, ApplicationEventPublisher events) {
        this.products = products;
        this.events = events;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "products", allEntries = true)
    public void changeDescription(String rawSlug, String description) {
        Product product = Slug.parse(rawSlug)
                .flatMap(products::findBySlug)
                .orElseThrow(() -> new NotFoundException("Product not found: " + rawSlug));

        product.changeDescription(description);
        events.publishEvent(new ProductTextChanged(product.getId()));
    }

}
