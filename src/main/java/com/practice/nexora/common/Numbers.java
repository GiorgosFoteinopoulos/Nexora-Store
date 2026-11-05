package com.practice.nexora.common;

public final class Numbers {

    private Numbers() {
    }

    public static double requireBetween(double value, double min, double max, String field) {
        if (!(value >= min && value <= max)) {
            throw new IllegalArgumentException(
                    "%s must be between %s and %s: %s".formatted(field, min, max, value));
        }
        return value;
    }

    public static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException("%s must be positive: %d".formatted(field, value));
        }
        return value;
    }
}
