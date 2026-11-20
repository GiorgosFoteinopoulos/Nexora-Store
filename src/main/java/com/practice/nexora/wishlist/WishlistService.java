package com.practice.nexora.wishlist;


import com.practice.nexora.catalog.Product;
import com.practice.nexora.catalog.ProductRepository;
import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.common.Slug;
import com.practice.nexora.user.UserAccount;
import com.practice.nexora.user.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WishlistService {

    private final WishlistItemRepository items;
    private final UserAccountRepository users;
    private final ProductRepository products;

    public WishlistService(WishlistItemRepository items, UserAccountRepository users, ProductRepository products) {
        this.items = items;
        this.users = users;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> wishlistOf(String email) {
        return items.findByUserOrderByAddedAtDescIdDesc(findUser(email)).stream()
                .map(WishlistItemResponse::from)
                .toList();
    }

    public void add(String email, String slug) {
        UserAccount user = findUser(email);
        Product product = findProduct(slug);
        items.addIfMissing(user.getId(), product.getId());
    }

    public void remove(String email, String slug) {
        items.deleteByUserAndProduct(findUser(email), findProduct(slug));
    }

    private UserAccount findUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }
    private Product findProduct(String rawSlug) {
        return Slug.parse(rawSlug)
                .flatMap(products::findBySlug)
                .orElseThrow(() -> new NotFoundException("Product not found: " + rawSlug));
    }

}
