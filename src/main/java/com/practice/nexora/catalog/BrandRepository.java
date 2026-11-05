package com.practice.nexora.catalog;

import com.practice.nexora.common.Slug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findBySlug(Slug slug);

    boolean existsBySlug(Slug slug);
}
