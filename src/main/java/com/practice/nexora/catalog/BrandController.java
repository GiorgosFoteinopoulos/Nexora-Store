package com.practice.nexora.catalog;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@Tag(name = "Brands", description = "The brands in the catalog")
public class BrandController {

    private final BrandService brandService;

    BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    @Operation(summary = "List brands", description = "All brands, sorted by name.")
    List<BrandResponse> listBrands() {
        return brandService.listBrands();
    }
}
