package com.practice.nexora.review;

import java.util.List;

public record ProductReviewsResponse(Double averageRating, long reviewCount, List<ReviewResponse> reviews) {
}
