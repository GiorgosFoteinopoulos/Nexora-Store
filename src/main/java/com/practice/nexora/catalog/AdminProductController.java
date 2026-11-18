package com.practice.nexora.catalog;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Admin: catalog")
class AdminProductController {

    private final AdminProductService admin;
    private final ProductService products;

    AdminProductController(AdminProductService admin, ProductService products) {
        this.admin = admin;
        this.products = products;
    }

    @PatchMapping("/{slug}")
    @Operation(summary = "Change a product's description (ADMIN). Its vector is refreshed automatically after commit.")
    ProductResponse changeDescription(@PathVariable String slug, @Valid @RequestBody UpdateDescriptionRequest request) {
        admin.changeDescription(slug, request.description());
        return products.getProduct(slug);
    }
}
