package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.Optional;

import static com.practice.nexora.common.Text.requireText;


@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 32)
    private Sku sku;

    @Column(nullable = false, length = 40)
    private String colour;

    private Integer storageGb;

    @Embedded // Money's two fields become two columns of this table
    @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false, precision = 10, scale = 2))
    @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false, length = 3))
    private Money price;

    private int stock;

    protected ProductVariant() {

    }

    public ProductVariant(Sku sku, String colour, Integer storageGb, Money price, int stock) {
        this.sku = Objects.requireNonNull(sku, "sku must not be null");
        this.colour = requireText(colour, "colour");
        if (storageGb != null && storageGb <= 0) {
            throw new IllegalArgumentException("storageGb must be positive: " + storageGb);
        }
        this.storageGb = storageGb;
        this.price = Objects.requireNonNull(price, "price must not be null");
        if (stock < 0) {
            throw new IllegalArgumentException("stock must not be negative: " + stock);
        }
        this.stock = stock;
    }


    void attachTo(Product product) {
        Objects.requireNonNull(product, "product must not be null");
        if (this.product != null && !this.product.equals(product)) {
            throw new IllegalStateException(
                    "%s already belongs to %s".formatted(sku, this.product.getName()));
        }
        this.product = product;
    }



    public void restock(int quantity) {
        requirePositive(quantity);
        stock = Math.addExact(stock, quantity);
    }

    public void removeFromStock(int quantity) {
        requirePositive(quantity);
        if (quantity > stock) {
            throw new IllegalStateException(
                    "Cannot remove %d of %s: only %d in stock".formatted(quantity, sku, stock));
        }
        stock -= quantity;
    }

    public void changePrice(Money newPrice) {
        Objects.requireNonNull(newPrice, "newPrice must not be null");
        if (!newPrice.currency().equals(price.currency())) {
            throw new IllegalArgumentException(
                    "Price must stay in %s, got %s".formatted(price.currency(), newPrice.currency()));
        }
        price = newPrice;
    }

    public boolean isInStock() {
        return stock > 0;
    }



    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Sku getSku() {
        return sku;
    }

    public String getColour() {
        return colour;
    }

    public Optional<Integer> getStorageGb() {
        return Optional.ofNullable(storageGb);
    }

    public Money getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductVariant other)) {
            return false;
        }
        return sku.equals(other.getSku());
    }

    @Override
    public int hashCode() {
        return sku.hashCode();
    }

    @Override
    public String toString() {
        return "ProductVariant[%s, %s, %s, stock=%d]".formatted(sku, colour, price.amount(), stock);
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
    }
}