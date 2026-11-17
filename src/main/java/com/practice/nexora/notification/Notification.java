package com.practice.nexora.notification;

import com.practice.nexora.user.UserAccount;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "price_change_id", nullable = false)
    private Long priceChangeId;


    @Column(name = "product_slug", nullable = false, length = 80)
    private String productSlug;

    @Column(nullable = false, length = 300)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {

    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }
    public Long getPriceChangeId() {
        return priceChangeId;
    }
    public String getProductSlug() {
        return productSlug;
    }
    public String getMessage() {
        return message;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public boolean isRead() {
        return readAt != null;
    }
}
