package com.practice.nexora.review;

import java.time.Instant;

public record ReviewResponse(Long id, String author, int rating, String comment, Instant createdAt) {

    static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                mask(review.getAuthor().getEmail()),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
    static String mask(String email) {
        return email.charAt(0) + "***";
    }
}
