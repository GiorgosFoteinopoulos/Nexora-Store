package com.practice.nexora.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.practice.nexora.common.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/products")
class ProductController {

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{slug}")
    ProductResponse getProduct(@PathVariable String slug) {
        return productService.getProduct(slug);
    }

    @GetMapping
    PageResponse<ProductSummaryResponse> listProducts(ProductFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return productService.listProducts(filter, pageable);
    }
}
