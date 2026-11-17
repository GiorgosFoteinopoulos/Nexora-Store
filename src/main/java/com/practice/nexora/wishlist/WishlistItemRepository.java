package com.practice.nexora.wishlist;

import com.practice.nexora.catalog.Product;
import com.practice.nexora.user.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @EntityGraph(attributePaths = "product")
    List<WishlistItem> findByUserOrderByAddedAtDescIdDesc(UserAccount user);

    @Modifying
    @Query(value = """
            insert into wishlist_items (user_id, product_id, added_at)
            values (:userId, :productId, now())
            on conflict (user_id, product_id) do nothing
""", nativeQuery = true)
    int addIfMissing(@Param("userId") Long userId, @Param("productId") Long productId);

    long deleteByUserAndProduct(UserAccount user, Product product);
}
