package com.practice.nexora.review;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviews;

    ReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping("/api/products/{slug}/reviews")
    @Operation(summary = "Reviews of a product (public), newest first, with average rating and count")
    ProductReviewsResponse list(@PathVariable String slug) {
        return reviews.reviewsOf(slug);
    }

    @PostMapping("/api/products/{slug}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Write a review (logged-in users; one per user per product)")
    ReviewResponse add(@PathVariable String slug,
                       @AuthenticationPrincipal Jwt jwt,
                       @Valid @RequestBody CreateReviewRequest request) {
        return reviews.addReview(slug, jwt.getSubject(), request);
    }

    @DeleteMapping("/api/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a review (its author, or an admin)")
    void delete(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        reviews.deleteReview(id, authentication.getName(), isAdmin);
    }
}
