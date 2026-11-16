package com.practice.nexora.review;


import com.practice.nexora.catalog.Product;
import com.practice.nexora.catalog.ProductRepository;
import com.practice.nexora.common.ConflictException;
import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.common.Slug;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    static final String ALREADY_REVIEWED = "You have already reviewed this product";

    private final ReviewRepository reviews;
    private final ProductRepository products;
    private final UserAccountRepository users;

    public ReviewService(ReviewRepository reviews, ProductRepository products, UserAccountRepository users) {
        this.reviews = reviews;
        this.products = products;
        this.users = users;
    }
    public ReviewResponse addReview(String slug, String authorEmail, CreateReviewRequest request) {
        Product product = findProduct(slug);
        UserAccount author = users.findByEmail(authorEmail)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (reviews.existsByProductAndAuthor(product, author)) {
            throw new ConflictException(ALREADY_REVIEWED);
        }
        try {
            Review saved = reviews.saveAndFlush(new Review(product, author, request.rating(), request.comment()));
            return ReviewResponse.from(saved);
        } catch (DataIntegrityViolationException raceLost) {
            throw new ConflictException(ALREADY_REVIEWED);
        }
    }

    @Transactional(readOnly = true)
    public ProductReviewsResponse reviewsOf(String slug) {
        Product product = findProduct(slug);
        List<ReviewResponse> list = reviews.findByProductOrderByCreatedAtDescIdDesc(product).stream()
                .map(ReviewResponse::from)
                .toList();
        return new ProductReviewsResponse(
                oneDecimal(reviews.averageRating(product)),
                reviews.countByProduct(product),
                list
        );
    }

    private Product findProduct(String rawSlug) {
        return parseSlug(rawSlug)
                .flatMap(products::findBySlug)
                .orElseThrow(() -> new NotFoundException("Product not found: " + rawSlug));
    }

    private static Optional<Slug> parseSlug(String raw) {
        try {
            return Optional.of(new Slug(raw));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    private static Double oneDecimal(Double value) {
        return value == null ? null : Math.round(value * 10) / 10.0;
    }
}
