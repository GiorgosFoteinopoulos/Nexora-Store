package com.practice.nexora.review;


import com.practice.nexora.catalog.Product;
import com.practice.nexora.user.UserAccount;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "reviews")
public class Review {

    static final int MIN_RATING = 1;
    static final int MAX_RATING = 5;
    static final int MAX_COMMENT_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount author;

    @Column(nullable = false)
    private int rating;

    @Column(length = MAX_COMMENT_LENGTH)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Review() {

    }

    public Review(Product product, UserAccount author, int rating, String comment) {
        this.product = Objects.requireNonNull(product, "product cannot be null");
        this.author = Objects.requireNonNull(author, "author cannot be null");
        this.rating = requireValidRating(rating);
        this.comment = cleanComment(comment);
        this.createdAt = Instant.now();
    }

    private static int requireValidRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                    "rating must be between " + MIN_RATING + " and " + MAX_RATING + ", but was " + rating
            );

        }
        return rating;
    }

    private static String cleanComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        String stripped = comment.strip();
        if (stripped.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("comment must be at most " + MAX_COMMENT_LENGTH + " characters");
        }
        return stripped;
    }

    public boolean isWrittenBy(String email) {
        return author.getEmail().equals(email);
    }

    public Long getId() {
        return id;
    }
    public Product getProduct() {
        return product;
    }
    public UserAccount getAuthor() {
        return author;
    }
    public int getRating() {
        return rating;
    }
    public String getComment() {
        return comment;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
