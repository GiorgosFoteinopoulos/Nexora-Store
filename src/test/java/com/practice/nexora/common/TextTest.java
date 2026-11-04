package com.practice.nexora.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.practice.nexora.common.Text.requireText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TextTest {

    @Test
    void returnsTheValueWithoutSurroundingWhitespace() {
        assertThat(requireText(" Nexora Phone ", "name")).isEqualTo("Nexora Phone");
    }

    @ParameterizedTest(name = "rejects blank value #{index}")
    @ValueSource(strings = {"", "  ", "\t\n"})
    void rejectsBlankValues(String value) {
        assertThatThrownBy(() -> requireText(value, "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be blank");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> requireText(null, "name"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("name must not be null");
    }
}
