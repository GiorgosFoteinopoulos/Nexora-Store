package com.practice.nexora.catalog;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record Sku(String value) {

    private static final Pattern FORMAT = Pattern.compile("[A-Z0-9]+(-[A-Z0-9]+)*");
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 32;

    public Sku {
        Objects.requireNonNull(value, "SKU must not be null");
        value = value.strip().toUpperCase(Locale.ROOT);

        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "SKU must be %d to %d characters long: %s'".formatted(MIN_LENGTH, MAX_LENGTH, value));
        }
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "SKU may only contain letters, digits and single hyphens between them: '" + value + "'");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
