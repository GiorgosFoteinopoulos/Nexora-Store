package com.practice.nexora.catalog;

import com.practice.nexora.common.Slug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(Slug slug);

    List<Product> findByBrand(Brand brand);
}
