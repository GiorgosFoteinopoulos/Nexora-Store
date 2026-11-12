package com.practice.nexora.catalog;

import com.practice.nexora.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Catalog", description = "Browse and search products")
@RestController
@RequestMapping("/api/products")
class ProductController {

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "List products",
            description = "One page of product summaries, sorted by name. Every filter is optional; filters combine with AND.")
    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page number, counting from 0",
            schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Products per page (at most 100)",
            schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100"))
    @ApiResponse(responseCode = "200", description = "A page of products", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "A filter value has the wrong format",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    PageResponse<ProductSummaryResponse> listProducts(
            @ParameterObject ProductFilter filter,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return productService.listProducts(filter, pageable);
    }

    @Operation(summary = "Get one product", description = "The full product page, with every variant.")
    @ApiResponse(responseCode = "200", description = "The product", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "404", description = "No product with this slug",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{slug}")
    ProductResponse getProduct(
            @Parameter(description = "The product's URL name", example = "nexora-phone-15") @PathVariable String slug) {
        return productService.getProduct(slug);
    }
}