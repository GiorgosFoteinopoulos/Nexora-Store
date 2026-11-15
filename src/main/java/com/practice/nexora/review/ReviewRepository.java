package com.practice.nexora.review;

import com.practice.nexora.catalog.Product;
import com.practice.nexora.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByProductAndAuthor(Product product, UserAccount author);

    long countByProduct(Product product);

    @Query("select avg(r.rating) from Review r where r.product = :product")
    Double averageRating(@Param("product") Product product);
}
