package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;
import com.practice.nexora.common.Slug;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.practice.nexora.common.Text.requireText;


@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "category", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 80)
    private Slug slug;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false, length = 2000)
    private String description;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ProductVariant> variants = new ArrayList<>();

    protected Product() {

    }

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
                    "%s already has a variant with SKU %s".formatted(name, variant.getSku()));
        }
        if (!variants.isEmpty()) {
            var currency = variants.getFirst().getPrice().currency();
            if (!variant.getPrice().currency().equals(currency)) {
                throw new IllegalArgumentException(
                        "All variants of %s must be priced in %s".formatted(name, currency));
            }
        }
        variant.attachTo(this);
        variants.add(variant);
    }

    public void removeVariant(Sku sku) {
        ProductVariant variant = findVariant(sku).orElseThrow(() -> new IllegalArgumentException(
                "%s has no variant with SKU %s".formatted(name, sku)));
        variants.remove(variant);
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

    public void changeDescription(String newDescription) {
        this.description = requireText(newDescription, "description");
    }



    public Long getId() {
        return id;
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product other)) {
            return false;
        }
        return slug.equals(other.getSlug());
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