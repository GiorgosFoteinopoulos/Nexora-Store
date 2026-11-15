package com.practice.nexora.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceChangeRepository extends JpaRepository<PriceChange, Long> {

    List<PriceChange> findByVariant_SkuOrderByChangedAtAscIdAsc(Sku sku);

    long countByVariant_Sku(Sku sku);
}
