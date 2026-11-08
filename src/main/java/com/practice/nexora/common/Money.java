package com.practice.nexora.common;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;


@Embeddable
public record Money(BigDecimal amount, Currency currency) {

    public static final Currency EUR = Currency.getInstance("EUR");

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
        int digits = currency.getDefaultFractionDigits();
        if (amount.stripTrailingZeros().scale() > digits) {
            throw new IllegalArgumentException(
                    "%s allows at most %d decimal places: %s".formatted(currency, digits, amount)
            );
        }
        amount = amount.setScale(digits, RoundingMode.UNNECESSARY);
    }

    public static Money euros(String amount) {
        return new Money(new BigDecimal(amount), EUR);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot combine %s with %s".formatted(currency, other.currency)
            );
        }
    }
}
