package com.practice.nexora.catalog;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductSummaryResponse(
        String slug,
        String name,
        Category category,
        BrandResponse brand,
        PriceResponse startingPrice,
        boolean available,
        String summary) {


    static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getSlug().value(),
                product.getName(),
                product.getCategory(),
                BrandResponse.from(product.getBrand()),
                product.getStartingPrice().map(PriceResponse::from).orElse(null),
                product.isAvailable(),
                ProductSpecs.of(product).summary()
        );
    }
}
