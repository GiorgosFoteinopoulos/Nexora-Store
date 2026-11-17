package com.practice.nexora.wishlist;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/wishlist")
@Tag(name = "Wishlist")
public class WishlistController {

    private final WishlistService wishlist;

    WishlistController(WishlistService wishlist) {
        this.wishlist = wishlist;
    }

    @GetMapping
    @Operation(summary = "My wishlist, newest first")
    List<WishlistItemResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return wishlist.wishlistOf(jwt.getSubject());
    }

    @PutMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Add a product to my wishlist (idempotent)")
    void add(@AuthenticationPrincipal Jwt jwt, @PathVariable String slug) {
        wishlist.add(jwt.getSubject(), slug);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a product from my wishlist (idempotent)")
    void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable String slug) {
        wishlist.remove(jwt.getSubject(), slug);
    }
}
