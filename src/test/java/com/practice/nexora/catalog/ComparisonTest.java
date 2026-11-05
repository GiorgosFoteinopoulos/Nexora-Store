package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComparisonTest {

    private static final Brand NEXORA = new Brand("Nexora");
    private static final Brand VOLTIX = new Brand("Voltix");

    private static Phone phone(String name, Brand brand, String sku, String price) {
        Phone phone = new Phone(name, brand, "Text", 6.1, 4500, true);
        phone.addVariant(new ProductVariant(new Sku(sku), "Black", 128, Money.euros(price), 5));
        return phone;
    }

    private static List<Product> phones(int count) {
        return IntStream.rangeClosed(1, count)
                .<Product>mapToObj(i -> phone("Nexora Phone " + i, NEXORA, "NX-PH" + i, "999"))
                .toList();
    }

    @Nested
    class Creating {

        @ParameterizedTest(name = "accepts {0} products")
        @ValueSource(ints = {2, 3, 4})
        void acceptsTwoToFourProductsOfTheSameCategory(int count) {
            Comparison comparison = new Comparison(phones(count));

            assertThat(comparison.getProducts()).hasSize(count);
            assertThat(comparison.getCategory()).isEqualTo(Category.PHONE);
        }

        @ParameterizedTest(name = "rejects {0} products")
        @ValueSource(ints = {1, 5})
        void rejectsTooFewOrTooManyProducts(int count) {
            assertThatThrownBy(() -> new Comparison(phones(count)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("needs 2 to 4 products, got " + count);
        }

        @Test
        void rejectsTheSameProductTwice() {
            Product phone = phone("Nexora Phone 15", NEXORA, "NX-PH15", "999");

            assertThatThrownBy(() -> new Comparison(List.of(phone, phone)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("more than once");
        }

        @Test
        void rejectsProductsOfDifferentCategories() {
            Product phone = phone("Nexora Phone 15", NEXORA, "NX-PH15", "999");
            Product laptop = new Laptop("Nexora Book 14", NEXORA, "Text", 14.0, "Nexora N3", 16, 1.2);

            assertThatThrownBy(() -> new Comparison(List.of(phone, laptop)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("same category");
        }

        @Test
        void keepsItsOwnCopyOfTheProducts() {
            List<Product> callersList = new ArrayList<>(phones(2));
            Comparison comparison = new Comparison(callersList);

            callersList.add(phone("Nexora Phone 99", NEXORA, "NX-PH99", "999"));

            assertThat(comparison.getProducts()).hasSize(2);
            assertThatThrownBy(() -> comparison.getProducts().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class Table {

        @Test
        void rowsComeInAFixedOrder() {
            Map<String, List<String>> table = new Comparison(phones(2)).table();

            assertThat(table.keySet())
                    .containsExactly("Product", "Brand", "Price from", "Available", "Screen", "Battery", "5G");
        }

        @Test
        void hasOneColumnPerProductInTheGivenOrder() {
            Comparison comparison = new Comparison(List.of(
                    phone("Voltix V9", VOLTIX, "VX-V9", "1199"),
                    phone("Nexora Phone 15", NEXORA, "NX-PH15", "999")));

            Map<String, List<String>> table = comparison.table();

            assertThat(table.get("Product")).containsExactly("Voltix V9", "Nexora Phone 15");
            assertThat(table.get("Brand")).containsExactly("Voltix", "Nexora");
            assertThat(table.get("Price from")).containsExactly("1199.00 EUR", "999.00 EUR");
        }

        @Test
        void showsTheSpecificationsOfTheCategory() {
            Comparison laptops = new Comparison(List.of(
                    new Laptop("Nexora Book 14", NEXORA, "Text", 14.0, "Nexora N3", 16, 1.2),
                    new Laptop("Voltix Air 13", VOLTIX, "Text", 13.3, "Voltix V2", 8, 1.1)));

            Map<String, List<String>> table = laptops.table();

            assertThat(table).containsKeys("Processor", "Memory", "Weight");
            assertThat(table.get("Memory")).containsExactly("16 GB", "8 GB");
            assertThat(table.get("Screen")).containsExactly("14.0\"", "13.3\"");
        }

        @Test
        void showsADashWhenAProductHasNoPriceYet() {
            Product withoutVariants = new Phone("Nexora Phone 16", NEXORA, "Text", 6.3, 4700, true);
            Comparison comparison = new Comparison(List.of(
                    phone("Nexora Phone 15", NEXORA, "NX-PH15", "999"), withoutVariants));

            assertThat(comparison.table().get("Price from")).containsExactly("999.00 EUR", "-");
            assertThat(comparison.table().get("Available")).containsExactly("Yes", "No");
        }
    }

    @Nested
    class Cheapest {

        @Test
        void findsTheProductWithTheLowestStartingPrice() {
            Product expensive = phone("Voltix V9", VOLTIX, "VX-V9", "1199");
            Product cheap = phone("Nexora Phone 15", NEXORA, "NX-PH15", "999");

            assertThat(new Comparison(List.of(expensive, cheap)).cheapest()).contains(cheap);
        }

        @Test
        void isEmptyWhenNoProductHasAPrice() {
            Comparison comparison = new Comparison(List.of(
                    new Phone("Nexora Phone 16", NEXORA, "Text", 6.3, 4700, true),
                    new Phone("Voltix V10", VOLTIX, "Text", 6.5, 5000, true)));

            assertThat(comparison.cheapest()).isEmpty();
        }
    }
}