package com.practice.nexora.common;

import java.util.Objects;

public final class Text {

    private Text() {

    }

    public static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        String stripped = value.strip();
        if (stripped.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return stripped;
    }
}
