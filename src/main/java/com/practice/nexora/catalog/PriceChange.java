package com.practice.nexora.catalog;


import com.practice.nexora.common.Money;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

import static com.practice.nexora.common.Text.requireText;

@Entity
@Table(name = "price_history")
public class PriceChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "old_price_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "old_price_currency"))
    private Money oldPrice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "new_price_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "new_price_currency"))
    private Money newPrice;

    @Column(name = "changed_by", nullable = false, length = 254)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected PriceChange() {

    }

    PriceChange(ProductVariant variant, Money oldPrice, Money newPrice, String changedBy) {
        this.variant = Objects.requireNonNull(variant, "variant");
        this.oldPrice = Objects.requireNonNull(oldPrice, "oldPrice");
        this.newPrice = Objects.requireNonNull(newPrice, "newPrice");
        this.changedBy = requireText(changedBy, "changed by");
        this.changedAt = Instant.now();
    }

    public Money getOldPrice() {
        return oldPrice;
    }
    public Money getNewPrice() {
        return newPrice;
    }
    public String getChangedBy() {
        return changedBy;
    }
    public Instant getChangedAt() {
        return changedAt;
    }
}
