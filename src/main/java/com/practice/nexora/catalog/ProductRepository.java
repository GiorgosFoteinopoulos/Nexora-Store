package com.practice.nexora.catalog;

import com.practice.nexora.common.Slug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(Slug slug);

    List<Product> findByBrand(Brand brand);

    List<Product> findByBrand_SlugOrderByName(Slug brandSlug);

    @Query("SELECT p FROM Product p where TYPE(p) = :kind ORDER BY p.name")
    List<Product> findAllOfKind(@Param("kind") Class<? extends Product> kind);

    @Query("""
            SELECT DISTINCT p FROM Product p JOIN p.variants v
            WHERE v.stock > 0 AND v.price.amount <= :maxPrice
            ORDER BY p.name
            """)
    List<Product> findInStockAtOrBelow(@Param("maxPrice") BigDecimal maxPrice);
}
