package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;

public record PriceResponse(String amount, String currency) {

    static PriceResponse from(Money money) {
        return new PriceResponse(money.amount().toPlainString(), money.currency().getCurrencyCode());
    }
}
