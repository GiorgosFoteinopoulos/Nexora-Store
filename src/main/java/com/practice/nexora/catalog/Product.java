package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;
import com.practice.nexora.common.Slug;

import java.util.*;

import static com.practice.nexora.common.Text.requireText;

public abstract class Product {

    private final String name;
    private final Slug slug;
    private final Brand brand;
    private String description;
    private final List<ProductVariant> variants = new ArrayList<>();

    protected Product(String name, Brand brand, String description) {
        this.name = requireText(name, "product name");
        this.slug = Slug.from(this.name);
        this.brand = Objects.requireNonNull(brand, "brand must not be null");
        this.description = requireText(description, "description");
    }
    public abstract Category getCategory();


    public void addVariant(ProductVariant variant) {
        Objects.requireNonNull(variant, "variant must not be null");
        if (variants.contains(variant)) {
            throw new IllegalStateException(
                    "%s already has a variant with SKU %s".formatted(name, variant.getSku())
            );
        }
        if (!variants.isEmpty()) {
            var currency = variants.getFirst().getPrice().currency();
            if (!variant.getPrice().currency().equals(currency)) {
                throw new IllegalArgumentException(
                        "All variants of %s must be priced in %s".formatted(name, currency)
                );
            }
        }
        variants.add(variant);
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public Optional<ProductVariant> findVariant(Sku sku) {
        return variants.stream()
                .filter(variant -> variant.getSku().equals(sku))
                .findFirst();
    }

    public boolean isAvailable() {
        return variants.stream().anyMatch(ProductVariant::isInStock);
    }

    public int getTotalStock() {
        return variants.stream().mapToInt(ProductVariant::getStock).sum();
    }

    public Optional<Money> getStartingPrice() {
        return variants.stream()
                .map(ProductVariant::getPrice)
                .min(Comparator.comparing(Money::amount));
    }

    public void changeDescription(String description) {
        this.description = requireText(description, "description");
    }

    public String getName() {
        return name;
    }
    public Slug getSlug() {
        return slug;
    }
    public Brand getBrand() {
        return brand;
    }
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o ) {return true;}
        if (!(o instanceof Product other)) {return false;}
        return slug.equals(other.slug);
    }

    @Override
    public int hashCode() {
        return slug.hashCode();
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(name, getCategory());
    }


}
