package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;
import static com.practice.nexora.common.Text.requireText;

import java.util.Objects;
import java.util.Optional;

public class ProductVariant {

    private final Sku sku;
    private final String colour;
    private final Integer storageGb;

    private Money price;
    private int stock;

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
                    "Price must stay in %s, got %s".formatted(price, newPrice.currency())
            );
        }
        price = newPrice;
    }

    public boolean isInStock() {
        return stock > 0;
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
        return sku.equals(other.sku);
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
