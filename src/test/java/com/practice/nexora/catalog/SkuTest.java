package com.practice.nexora.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkuTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "NX-PH15-256-BLK,    NX-PH15-256-BLK",
            "nx-ph15-256-blk,    NX-PH15-256-BLK",
            "'  NX-LP14-1TB  ',  NX-LP14-1TB",
            "ABC,                ABC"
    })
    void storesValidCodesInCanonicalForm(String input, String expected) {
        assertThat(new Sku(input).value()).isEqualTo(expected);
    }

    @Test
    void codesThatDifferOnlyInCaseAreTheSameSku() {
        assertThat(new Sku("nx-ph15-256-blk")).isEqualTo(new Sku("NX-PH15-256-BLK"));
    }

    @ParameterizedTest(name = "rejects \"{0}\"")
    @ValueSource(strings = {
            "",
            "AB",
            "A23456789012345678901234567890123",
            "NX--PH15",
            "-NX-PH15",
            "NX-PH15-",
            "NX PH15",
            "NX_PH15",
            "ΝΧ-ΡΗ15"
    })
    void rejectsInvalidCodes(String input) {
        assertThatThrownBy(() -> new Sku(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new Sku(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void printsJustTheCode() {
        assertThat(new Sku("NX-PH15-256-BLK")).hasToString("NX-PH15-256-BLK");
    }
}