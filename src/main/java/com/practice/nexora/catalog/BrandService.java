package com.practice.nexora.catalog;


import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BrandService {

    private static final Sort BY_NAME = Sort.by("name");

    private final BrandRepository brands;

    public BrandService(BrandRepository brands) {
        this.brands = brands;
    }

    public List<BrandResponse> listBrands() {
        return brands.findAll(BY_NAME).stream()
                .map(BrandResponse::from)
                .toList();
    }
}
