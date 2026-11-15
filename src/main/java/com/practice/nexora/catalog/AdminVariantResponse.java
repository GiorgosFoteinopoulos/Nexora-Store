package com.practice.nexora.catalog;

public record AdminVariantResponse(String sku, PriceResponse price, int stock, long version) {

    static AdminVariantResponse from(ProductVariant variant) {
        return new AdminVariantResponse(
                variant.getSku().value(),
                PriceResponse.from(variant.getPrice()),
                variant.getStock(),
                variant.getVersion());
    }
}
