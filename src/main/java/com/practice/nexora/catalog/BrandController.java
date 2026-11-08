package com.practice.nexora.catalog;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    List<BrandResponse> listBrands() {
        return brandService.listBrands();
    }
}
