package com.practice.nexora.catalog;

public record BrandResponse(String name, String slug) {

    static BrandResponse from(Brand brand) {
        return new BrandResponse(brand.getName(), brand.getSlug().value());
    }
}
