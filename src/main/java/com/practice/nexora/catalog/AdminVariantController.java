package com.practice.nexora.catalog;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/admin/variants")
class AdminVariantController {

    private final AdminCatalogService adminCatalogService;

    AdminVariantController(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }

    @Operation(summary = "Read a variant (with its version)", security = @SecurityRequirement(name = "bearer"))
    @GetMapping("/{sku}")
    AdminVariantResponse getVariant(@PathVariable String sku) {
        return adminCatalogService.getVariant(sku);
    }

    @Operation(summary = "Change price and/or stock", security = @SecurityRequirement(name = "bearer"))
    @ApiResponse(responseCode = "200", description = "Changed. The answer has the new version", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "409", description = "Someone else changed it first: Reload and try again",
    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping("/{sku}")
    AdminVariantResponse updateVariant(@PathVariable String sku, @Valid @RequestBody UpdateVariantRequest request) {
        return adminCatalogService.updateVariant(sku, request);
    }
}
