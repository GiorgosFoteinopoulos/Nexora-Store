package com.practice.nexora.wishlist;

import java.time.Instant;

public record WishlistItemResponse(String slug, String name, Instant addedAt) {

    static WishlistItemResponse from(WishlistItem item) {
        return new WishlistItemResponse(
                item.getProduct().getSlug().value(),
                item.getProduct().getName(),
                item.getAddedAt()
        );
    }
}
