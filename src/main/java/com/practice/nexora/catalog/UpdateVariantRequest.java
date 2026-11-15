package com.practice.nexora.catalog;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateVariantRequest(
        @Positive BigDecimal price,
        @PositiveOrZero Integer stock,
        @NotNull Long version) {
}
