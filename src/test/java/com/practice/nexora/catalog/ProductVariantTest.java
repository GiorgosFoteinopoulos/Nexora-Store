package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductVariantTest {

    private static final Sku SKU = new Sku("NX-PH15-256-BLK");

    private static ProductVariant variantWithStock(int stock) {
        return new ProductVariant(SKU, "Black", 256, Money.euros("999"), stock);
    }

    @Nested
    class Creating {

        @Test
        void keepsItsValues() {
            ProductVariant variant =
                    new ProductVariant(SKU, "  Midnight Black ", 256, Money.euros("999"), 5);

            assertThat(variant.getSku()).isEqualTo(SKU);
            assertThat(variant.getColour()).isEqualTo("Midnight Black");
            assertThat(variant.getStorageGb()).contains(256);
            assertThat(variant.getPrice()).isEqualTo(Money.euros("999"));
            assertThat(variant.getStock()).isEqualTo(5);
        }

        @Test
        void storageIsOptional() {
            ProductVariant phoneCase =
                    new ProductVariant(new Sku("NX-CASE15-CLR"), "Clear", null, Money.euros("29.99"), 0);

            assertThat(phoneCase.getStorageGb()).isEmpty();
        }

        @ParameterizedTest(name = "rejects colour \"{0}\"")
        @ValueSource(strings = {"", "   "})
        void rejectsBlankColour(String colour) {
            assertThatThrownBy(() -> new ProductVariant(SKU, colour, 256, Money.euros("999"), 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("colour");
        }

        @ParameterizedTest(name = "rejects storage {0} GB")
        @ValueSource(ints = {0, -64})
        void rejectsNonPositiveStorage(int storageGb) {
            assertThatThrownBy(() -> new ProductVariant(SKU, "Black", storageGb, Money.euros("999"), 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("storageGb");
        }

        @Test
        void rejectsNegativeInitialStock() {
            assertThatThrownBy(() -> variantWithStock(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("stock");
        }
    }

    @Nested
    class Stock {

        @Test
        void restockAddsUnits() {
            ProductVariant variant = variantWithStock(3);

            variant.restock(5);

            assertThat(variant.getStock()).isEqualTo(8);
        }

        @Test
        void removeFromStockSubtractsUnits() {
            ProductVariant variant = variantWithStock(8);

            variant.removeFromStock(3);

            assertThat(variant.getStock()).isEqualTo(5);
        }

        @Test
        void theLastUnitCanBeRemoved() {
            ProductVariant variant = variantWithStock(1);

            variant.removeFromStock(1);

            assertThat(variant.getStock()).isZero();
            assertThat(variant.isInStock()).isFalse();
        }

        @Test
        void refusesToRemoveMoreThanAvailableAndChangesNothing() {
            ProductVariant variant = variantWithStock(2);

            assertThatThrownBy(() -> variant.removeFromStock(3))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("only 2 in stock");
            assertThat(variant.getStock()).isEqualTo(2); // failure atomicity
        }

        @ParameterizedTest(name = "rejects quantity {0}")
        @ValueSource(ints = {0, -1})
        void rejectsNonPositiveQuantities(int quantity) {
            ProductVariant variant = variantWithStock(10);

            assertThatThrownBy(() -> variant.restock(quantity))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> variant.removeFromStock(quantity))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void refusesToOverflow() {
            ProductVariant variant = variantWithStock(Integer.MAX_VALUE);

            assertThatThrownBy(() -> variant.restock(1))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    class Pricing {

        @Test
        void changesThePrice() {
            ProductVariant variant = variantWithStock(1);

            variant.changePrice(Money.euros("899"));

            assertThat(variant.getPrice()).isEqualTo(Money.euros("899"));
        }

        @Test
        void refusesAnotherCurrencyAndKeepsTheOldPrice() {
            ProductVariant variant = variantWithStock(1);
            Money dollars = new Money(new BigDecimal("899"), Currency.getInstance("USD"));

            assertThatThrownBy(() -> variant.changePrice(dollars))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EUR");
            assertThat(variant.getPrice()).isEqualTo(Money.euros("999"));
        }
    }

    @Nested
    class Identity {

        @Test
        void sameSkuMeansSameVariant() {
            ProductVariant before = variantWithStock(50);
            ProductVariant after = variantWithStock(1);

            assertThat(before).isEqualTo(after);
            assertThat(before.hashCode()).isEqualTo(after.hashCode());
        }

        @Test
        void differentSkuMeansDifferentVariant() {
            ProductVariant black = variantWithStock(1);
            ProductVariant white =
                    new ProductVariant(new Sku("NX-PH15-256-WHT"), "White", 256, Money.euros("999"), 1);

            assertThat(black).isNotEqualTo(white);
        }

        @Test
        void aSetHoldsEachVariantOnce() {
            Set<ProductVariant> variants = new HashSet<>();

            variants.add(variantWithStock(50));
            variants.add(variantWithStock(1));

            assertThat(variants).hasSize(1);
        }
    }
}