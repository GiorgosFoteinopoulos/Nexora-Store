package com.practice.nexora.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.practice.nexora.common.Numbers.requireBetween;
import static com.practice.nexora.common.Numbers.requirePositive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumbersTest {

    @ParameterizedTest(name = "accepts {0}")
    @ValueSource(doubles = {3.0, 8.0})
    void acceptsValuesOnTheLimits(double value) {
        assertThat(requireBetween(value, 3.0, 8.0, "size")).isEqualTo(value);
    }

    @ParameterizedTest(name = "rejects {0}")
    @ValueSource(doubles = {2.9, 8.1, Double.NaN})
    void rejectsValuesOutsideTheRangeIncludingNaN(double value) {
        assertThatThrownBy(() -> requireBetween(value, 3.0, 8.0, "size"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size must be between 3.0 and 8.0");
    }

    @Test
    void acceptsPositiveNumbers() {
        assertThat(requirePositive(1, "count")).isEqualTo(1);
    }

    @ParameterizedTest(name = "rejects {0}")
    @ValueSource(ints = {0, -1})
    void rejectsZeroAndNegativeNumbers(int value) {
        assertThatThrownBy(() -> requirePositive(value, "count"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("count must be positive: " + value);
    }
}