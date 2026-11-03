package com.practice.nexora.common;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency JPY = Currency.getInstance("JPY");

    @Nested
    class Creating {

        @Test
        void normalisesToTheCurrencysDecimalPlaces() {

            Money price = Money.euros("799");


            assertThat(price.amount()).isEqualTo(new BigDecimal("799.00"));
            assertThat(price.amount().scale()).isEqualTo(2);
        }

        @Test
        void bigDecimalEqualsAlsoComparesScale() {
            BigDecimal a = new BigDecimal("10.5");
            BigDecimal b = new BigDecimal("10.50");

            assertThat(a).isNotEqualTo(b);
            assertThat(a.compareTo(b)).isZero();
        }

        @Test
        void amountsWithDifferentScaleAreEqual() {
            Money a = Money.euros("10.5");
            Money b = Money.euros("10.50");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @ParameterizedTest
        @ValueSource(strings = {"-0.01", "-1", "-999.99"})
        void rejectsNegativeAmounts(String amount) {
            assertThatThrownBy(() -> Money.euros(amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        void rejectsMoreDecimalsThanTheCurrencyAllows() {
            assertThatThrownBy(() -> Money.euros("9.999"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("decimal places");
        }

        @Test
        void respectsCurrenciesWithoutDecimals() {
            Money yen = new Money(new BigDecimal("100"), JPY);

            assertThat(yen.amount().scale()).isZero();
            assertThatThrownBy(() -> new Money(new BigDecimal("100.5"), JPY))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsMissingValues() {
            assertThatThrownBy(() -> new Money(null, Money.EUR))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("amount");
            assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("currency");
        }
    }

    @Nested
    class Arithmetic {

        @Test
        void addsAmounts() {
            Money result = Money.euros("799.00").plus(Money.euros("0.99"));

            assertThat(result).isEqualTo(Money.euros("799.99"));
        }

        @Test
        void subtractsAmounts() {
            Money result = Money.euros("100").minus(Money.euros("19.99"));

            assertThat(result).isEqualTo(Money.euros("80.01"));
        }

        @Test
        void refusesToGoBelowZero() {
            assertThatThrownBy(() -> Money.euros("10").minus(Money.euros("10.01")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        void refusesToMixCurrencies() {
            Money dollars = new Money(new BigDecimal("10"), USD);

            assertThatThrownBy(() -> Money.euros("10").plus(dollars))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot combine");
        }

        @Test
        void neverChangesTheOriginal() {
            Money original = Money.euros("50");

            original.plus(Money.euros("25"));

            assertThat(original).isEqualTo(Money.euros("50"));
        }
    }

    @Nested
    class Comparing {

        @Test
        void comparesAmountsInTheSameCurrency() {
            Money cheap = Money.euros("499");
            Money expensive = Money.euros("1299");

            assertThat(expensive.isGreaterThan(cheap)).isTrue();
            assertThat(cheap.isGreaterThan(expensive)).isFalse();
            assertThat(cheap.isGreaterThan(Money.euros("499.00"))).isFalse();
        }
    }
}