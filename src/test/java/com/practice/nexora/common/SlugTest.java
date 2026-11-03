package com.practice.nexora.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlugTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "Nexora Phone 15 Pro,          nexora-phone-15-pro",
            "'  Nexora   Book  Air  ',     nexora-book-air",
            "Café Edition,                 cafe-edition",
            "Über-Phone 2 (2026),          uber-phone-2-2026",
            "Nexora Watch S3 – 45mm,       nexora-watch-s3-45mm"
    })
    void buildsSlugsFromNames(String name, String expected) {
        assertThat(Slug.from(name).value()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "cannot slugify \"{0}\"")
    @ValueSource(strings = {"", "   ", "!!!", "Κινητό"})
    void refusesTextWithoutLatinLettersOrDigits(String name) {
        assertThatThrownBy(() -> Slug.from(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot build a slug");
    }

    @ParameterizedTest(name = "accepts \"{0}\"")
    @ValueSource(strings = {"nexora-phone-15-pro", "a", "x1"})
    void acceptsValidSlugs(String value) {
        assertThat(new Slug(value).value()).isEqualTo(value);
    }

    @ParameterizedTest(name = "rejects \"{0}\"")
    @ValueSource(strings = {"", "Nexora-Phone", "nexora--phone", "-nexora", "nexora-", "nexora phone", "café"})
    void rejectsInvalidSlugs(String value) {
        assertThatThrownBy(() -> new Slug(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid slug");
    }

    @Test
    void rejectsSlugsLongerThan80Characters() {
        assertThatThrownBy(() -> new Slug("a".repeat(81)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}