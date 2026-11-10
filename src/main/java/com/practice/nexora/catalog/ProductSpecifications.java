package com.practice.nexora.catalog;

import com.practice.nexora.common.Slug;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ProductSpecifications {

    private ProductSpecifications() {

    }

    static Specification<Product> matching(ProductFilter filter) {
        List<Specification<Product>> conditions = new ArrayList<>();
        if (filter.category() != null) {
            conditions.add(inCategory(filter.category()));
        }
        if (filter.brand() != null) {
            conditions.add(ofBrand(filter.brand()));
        }
        if (filter.minPrice() != null || filter.maxPrice() != null) {
            conditions.add(priceBetween(filter.minPrice(), filter.maxPrice()));
        }
        if (filter.q() != null) {
            conditions.add(mentioning(filter.q()));
        }
        return Specification.allOf(conditions);
    }

    static Specification<Product> inCategory(Category category) {
        Class<? extends Product> type = entityClassOf(category);
        return (root, query, cb) -> cb.equal(root.type(), type);
    }

    static Specification<Product> ofBrand(String brand) {
        Slug slug;
        try {
            slug = new Slug(brand);
        } catch (IllegalArgumentException invalid) {
            return (root, query, cb) -> cb.disjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("brand").get("slug"), slug);
    }

    static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            Subquery<Integer> matchingVariant = query.subquery(Integer.class);
            Root<ProductVariant> variant = matchingVariant.from(ProductVariant.class);

            List<Predicate> where = new ArrayList<>();
            where.add(cb.equal(variant.get("product"), root)); // correlate: a variant of THIS product
            if (min != null) {
                where.add(cb.greaterThanOrEqualTo(variant.get("price").<BigDecimal>get("amount"), min));
            }
            if (max != null) {
                where.add(cb.lessThanOrEqualTo(variant.get("price").<BigDecimal>get("amount"), max));
            }
            matchingVariant.select(cb.literal(1)).where(where.toArray(Predicate[]::new));
            return cb.exists(matchingVariant);
        };
    }

    static Specification<Product> mentioning(String text) {
        String pattern = "%" + escapeLike(text.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.<String>get("name")), pattern, '\\'),
                cb.like(cb.lower(root.<String>get("description")), pattern, '\\')
        );
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static Class<? extends Product> entityClassOf(Category category) {
        return switch (category) {
            case PHONE -> Phone.class;
            case LAPTOP ->  Laptop.class;
            case TABLET -> Tablet.class;
            case WEARABLE -> Wearable.class;
            case ACCESSORY ->  Accessory.class;
        };
    }
}
