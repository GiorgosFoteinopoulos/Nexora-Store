package com.practice.nexora.common;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record Slug (String value) {

    private static final Pattern FORMAT = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");
    private static final Pattern ACCENTS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-|-$");
    private static final int MAX_LENGTH = 80;

    public Slug {
        Objects.requireNonNull(value, "value must not be null");
        if (value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid slug: '" + value + "'" );
        }
    }

    public static Slug from(String text) {
        Objects.requireNonNull(text, "next must not be null");

        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutAccents = ACCENTS.matcher(decomposed).replaceAll("");
        String lower = withoutAccents.toLowerCase(Locale.ROOT);
        String hyphenated = NON_ALPHANUMERIC.matcher(lower).replaceAll("-");
        String trimmed = EDGE_HYPHENS.matcher(hyphenated).replaceAll("");

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Cannot build a slug from: '" + text + "'");
        }
        return new Slug(trimmed);
    }

    @Override
    public String toString() {
        return value;
    }
}
