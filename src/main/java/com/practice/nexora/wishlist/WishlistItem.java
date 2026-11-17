package com.practice.nexora.wishlist;


import com.practice.nexora.catalog.Product;
import com.practice.nexora.user.UserAccount;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "wishlist_items")
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    protected WishlistItem() {

    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }
    public Product getProduct() {
        return product;
    }
    public Instant getAddedAt() {
        return addedAt;
    }
}
