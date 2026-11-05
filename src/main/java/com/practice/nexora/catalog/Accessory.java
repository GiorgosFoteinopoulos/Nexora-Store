package com.practice.nexora.catalog;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;


public class Accessory extends Product {

    private final Set<Category> compatibleWith;

    public Accessory(String name, Brand brand, String description, Set<Category> compatibleWith) {
        super(name, brand, description);
        Objects.requireNonNull(compatibleWith, "compatibleWith must not be null");

        EnumSet<Category> copy = EnumSet.noneOf(Category.class);
        copy.addAll(compatibleWith);

        if (copy.isEmpty()) {
            throw new IllegalArgumentException("An accessory must fit at least one category");
        }
        if (copy.contains(Category.ACCESSORY)) {
            throw new IllegalArgumentException("An accessory cannot be made for other accessories");
        }
        this.compatibleWith = copy;
    }

    @Override
    public Category getCategory() {
        return Category.ACCESSORY;
    }

    public boolean fits(Product product) {
        Objects.requireNonNull(product, "product must not be null");
        return compatibleWith.contains(product.getCategory());
    }
    
    public Set<Category> getCompatibleWith() {
        return Collections.unmodifiableSet(compatibleWith);
    }
}