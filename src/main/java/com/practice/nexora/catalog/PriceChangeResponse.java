package com.practice.nexora.catalog;

import java.time.Instant;

public record PriceChangeResponse(PriceResponse oldPrice, PriceResponse newPrice, String changedBy, Instant changedAt) {

    static PriceChangeResponse from(PriceChange change) {
        return new PriceChangeResponse(
                PriceResponse.from(change.getOldPrice()),
                PriceResponse.from(change.getNewPrice()),
                change.getChangedBy(),
                change.getChangedAt());
    }
}
