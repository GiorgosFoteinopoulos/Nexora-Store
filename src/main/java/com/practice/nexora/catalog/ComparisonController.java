package com.practice.nexora.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Compare")
class ComparisonController {

    private final ComparisonService comparisons;

    ComparisonController(ComparisonService comparisons) {
        this.comparisons = comparisons;
    }

    @GetMapping("/api/products/compare")
    @Operation(summary = "Compare 2 to 4 products of the same category side by side (public)")
    ComparisonResponse compare(@RequestParam List<String> slugs) {
        return comparisons.compare(slugs);
    }
}
