package com.practice.nexora.catalog;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
        String slug,
        String name,
        Category category,
        BrandResponse brand,
        String description,
        PriceResponse startingPrice,
        boolean available,
        String summary,
        ProductSpecs specs,
        List<Variant> variants) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Variant(String sku, String colour, Integer storageGb, PriceResponse price, boolean inStock) {

        static Variant from(ProductVariant variant) {
            return new Variant(
                    variant.getSku().value(),
                    variant.getColour(),
                    variant.getStorageGb().orElse(null),
                    PriceResponse.from(variant.getPrice()),
                    variant.isInStock());
        }
    }

    static ProductResponse from(Product product) {
        ProductSpecs specs = ProductSpecs.of(product);
        return new ProductResponse(
                product.getSlug().value(),
                product.getName(),
                product.getCategory(),
                BrandResponse.from(product.getBrand()),
                product.getDescription(),
                product.getStartingPrice().map(PriceResponse::from).orElse(null),
                product.isAvailable(),
                specs.summary(),
                specs,
                product.getVariants().stream().map(Variant::from).toList());
    }
}
