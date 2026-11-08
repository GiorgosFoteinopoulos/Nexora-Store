package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;
import com.practice.nexora.common.Slug;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests the behaviour every product shares, using a Phone because Product is abstract. */
class ProductTest {

    private static final Brand NEXORA = new Brand("Nexora");

    private static Product phone() {
        return new Phone("Nexora Phone 15 Pro", NEXORA,
                "Flagship phone with a 6.1-inch display.", 6.1, 4500, true);
    }

    private static ProductVariant variant(String sku, String price, int stock) {
        return new ProductVariant(new Sku(sku), "Black", 256, Money.euros(price), stock);
    }

    @Nested
    class Creating {

        @Test
        void derivesItsSlugAndKnowsItsCategory() {
            Product product = phone();

            assertThat(product.getSlug()).isEqualTo(new Slug("nexora-phone-15-pro"));
            assertThat(product.getBrand()).isEqualTo(NEXORA);
            assertThat(product.getCategory()).isEqualTo(Category.PHONE);
        }

        @Test
        void rejectsABlankName() {
            assertThatThrownBy(() -> new Phone("  ", NEXORA, "Text", 6.1, 4500, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("product name must not be blank");
        }

        @Test
        void rejectsAMissingBrand() {
            assertThatThrownBy(() -> new Phone("Nexora Phone 15", null, "Text", 6.1, 4500, true))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("brand");
        }

        @Test
        void startsWithoutVariants() {
            Product product = phone();

            assertThat(product.getVariants()).isEmpty();
            assertThat(product.isAvailable()).isFalse();
            assertThat(product.getStartingPrice()).isEmpty();
            assertThat(product.getTotalStock()).isZero();
        }
    }

    @Nested
    class Variants {

        @Test
        void addsVariantsAndFindsThemBySku() {
            Product product = phone();

            product.addVariant(variant("NX-PH15P-256-BLK", "1199", 3));
            product.addVariant(variant("NX-PH15P-512-BLK", "1399", 1));

            assertThat(product.getVariants()).hasSize(2);
            assertThat(product.findVariant(new Sku("NX-PH15P-512-BLK"))).isPresent();
        }

        @Test
        void findsNothingForAnUnknownSku() {
            assertThat(phone().findVariant(new Sku("NX-UNKNOWN"))).isEmpty();
        }

        @Test
        void refusesADuplicateSku() {
            Product product = phone();
            product.addVariant(variant("NX-PH15P-256-BLK", "1199", 3));

            assertThatThrownBy(() -> product.addVariant(variant("NX-PH15P-256-BLK", "999", 10)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already has a variant");
            assertThat(product.getVariants()).hasSize(1);
        }

        @Test
        void refusesAVariantPricedInAnotherCurrency() {
            Product product = phone();
            product.addVariant(variant("NX-PH15P-256-BLK", "1199", 3));
            ProductVariant inDollars = new ProductVariant(new Sku("NX-PH15P-256-WHT"), "White", 256,
                    new Money(new BigDecimal("1299"), Currency.getInstance("USD")), 2);

            assertThatThrownBy(() -> product.addVariant(inDollars))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EUR");
            assertThat(product.getVariants()).hasSize(1);
        }

        @Test
        void theVariantListCannotBeChangedFromOutside() {
            Product product = phone();

            assertThatThrownBy(() -> product.getVariants().add(variant("NX-SNEAKY", "1", 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThat(product.getVariants()).isEmpty();
        }
    }

    @Nested
    class ShopQuestions {

        @Test
        void startingPriceIsTheCheapestVariant() {
            Product product = phone();
            product.addVariant(variant("NX-PH15P-512-BLK", "1399", 1));
            product.addVariant(variant("NX-PH15P-256-BLK", "1199", 3));
            product.addVariant(variant("NX-PH15P-1TB-BLK", "1599", 0));

            assertThat(product.getStartingPrice()).contains(Money.euros("1199"));
        }

        @Test
        void isAvailableWhenAnyVariantIsInStock() {
            Product product = phone();
            product.addVariant(variant("NX-PH15P-256-BLK", "1199", 0));
            product.addVariant(variant("NX-PH15P-512-BLK", "1399", 3));

            assertThat(product.isAvailable()).isTrue();
        }

        @Test
        void isNotAvailableWhenEveryVariantIsSoldOut() {
            Product product = phone();
            product.addVariant(variant("NX-PH15P-256-BLK", "1199", 0));
            product.addVariant(variant("NX-PH15P-512-BLK", "1399", 0));

            assertThat(product.isAvailable()).isFalse();
        }

        @Test
        void totalStockAddsUpAllVariants() {
            Product product = phone();
            product.addVariant(variant("NX-PH15P-256-BLK", "1199", 3));
            product.addVariant(variant("NX-PH15P-512-BLK", "1399", 0));
            product.addVariant(variant("NX-PH15P-1TB-BLK", "1599", 7));

            assertThat(product.getTotalStock()).isEqualTo(10);
        }

        @Test
        void seesChangesMadeToItsVariants() {
            Product product = phone();
            product.addVariant(variant("NX-PH15P-256-BLK", "1199", 1));

            product.findVariant(new Sku("NX-PH15P-256-BLK")).orElseThrow().removeFromStock(1);

            assertThat(product.isAvailable()).isFalse();
        }
    }

    @Nested
    class Identity {

        @Test
        void sameNameMeansSameProduct() {
            Product a = phone();
            Product b = new Phone("Nexora Phone 15 Pro", NEXORA, "Other text", 6.7, 5000, false);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }

    @Test
    void removesAVariant() {
        Product product = phone();
        product.addVariant(variant("NX-PH15P-256-BLK", "1199", 3));
        product.addVariant(variant("NX-PH15P-512-BLK", "1399", 1));

        product.removeVariant(new Sku("NX-PH15P-256-BLK"));

        assertThat(product.getVariants())
                .extracting(ProductVariant::getSku)
                .containsExactly(new Sku("NX-PH15P-512-BLK"));
    }

    @Test
    void refusesToRemoveAnUnknownSku() {
        assertThatThrownBy(() -> phone().removeVariant(new Sku("NX-UNKNOWN")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NX-UNKNOWN");
    }

    @Test
    void aVariantCannotBelongToTwoProducts() {
        ProductVariant shared = variant("NX-SHARED", "999", 1);
        phone().addVariant(shared);
        Product other = new Phone("Nexora Phone 16", NEXORA, "Text", 6.3, 4700, true);

        assertThatThrownBy(() -> other.addVariant(shared))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already belongs");
        assertThat(other.getVariants()).isEmpty();
    }
}
