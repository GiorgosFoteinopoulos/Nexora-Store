package com.practice.nexora.catalog;

import com.practice.nexora.common.Slug;

import static com.practice.nexora.common.Text.requireText;

public class Brand {

    private final String name;
    private final Slug slug;

    public Brand(String name) {
        this.name = requireText(name, "brand name");
        this.slug = Slug.from(this.name);
    }

    public String getName() {
        return name;
    }
    public Slug getSlug() {
        return slug;
    }

    @Override
    public int hashCode() {
        return slug.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Brand other)) {
            return false;
        }
        return slug.equals(other.slug);
    }
}
