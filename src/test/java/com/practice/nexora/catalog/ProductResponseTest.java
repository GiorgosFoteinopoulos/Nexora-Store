package com.practice.nexora.catalog;

import com.practice.nexora.catalog.ProductSpecs.AccessorySpecs;
import com.practice.nexora.catalog.ProductSpecs.LaptopSpecs;
import com.practice.nexora.catalog.ProductSpecs.PhoneSpecs;
import com.practice.nexora.catalog.ProductSpecs.TabletSpecs;
import com.practice.nexora.catalog.ProductSpecs.WearableSpecs;
import com.practice.nexora.common.Money;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductResponseTest {

    private static final Brand NEXORA = new Brand("Nexora");

    private static Phone phone() {
        Phone phone = new Phone("Nexora Phone 15", NEXORA, "A phone.", 6.1, 4500, true);
        phone.addVariant(new ProductVariant(new Sku("NX-Ph15-256-BLK"), "Black", 256, Money.euros("899"), 0));
        phone.addVariant(new ProductVariant(new Sku("NX-PH15-128-BLK"), "Black", 128, Money.euros("799"), 5));
        return phone;
    }

    @Test
    void mapsTheFieldsEveryProductHas() {
        ProductResponse response = ProductResponse.from(phone());

        assertThat(response.slug()).isEqualTo("nexora-phone-15");
        assertThat(response.category()).isEqualTo(Category.PHONE);
        assertThat(response.brand()).isEqualTo(new BrandResponse("Nexora", "nexora"));
        assertThat(response.startingPrice()).isEqualTo(new PriceResponse("799.00", "EUR"));
        assertThat(response.available()).isTrue();
        assertThat(response.summary()).isEqualTo("6.1\" screen, 4500 mAh, 5G");
    }

    @Test
    void mapsTheVariantsWithoutRevealingStockLevels() {
        assertThat(ProductResponse.from(phone()).variants()).containsExactly(
                new ProductResponse.Variant("NX-PH15-256-BLK", "Black", 256, new PriceResponse("899.00", "EUR"), false),
                new ProductResponse.Variant("NX-PH15-128-BLK", "Black", 128, new PriceResponse("799.00", "EUR"), true));
    }
    @Test
    void eachKindGetsItsOwnSpecifications() {
        Laptop laptop = new Laptop("Nexora Book 14", NEXORA, "A laptop.", 14.0, "Nexora N3", 16, 1.2);

        assertThat(ProductSpecs.of(phone())).isEqualTo(new PhoneSpecs(6.1, 4500, true));
        assertThat(ProductSpecs.of(laptop)).isEqualTo(new LaptopSpecs(14.0, "Nexora N3", 16, 1.2, true));
    }

    @Test
    void accessoryCategoriesComeInAFixedOrder() {
        Accessory hub = new Accessory("Voltix USB-C Hub", NEXORA, "A hub.", Set.of(Category.TABLET, Category.LAPTOP));

        assertThat(ProductSpecs.of(hub)).isEqualTo(new AccessorySpecs(List.of(Category.LAPTOP, Category.TABLET)));
    }

    @Test
    void summariesDescribeEachKindInOneLine() {
        assertThat(new ProductSpecs.LaptopSpecs(14.0, "Nexora N3", 16, 1.2, true).summary())
                .isEqualTo("14.0\", Nexora N3, 16 GB RAM, 1.2 kg");
        assertThat(new ProductSpecs.TabletSpecs(11.0, true, false).summary()).isEqualTo("11.0\" screen, stylus");
        assertThat(new ProductSpecs.WearableSpecs(7, true, false).summary()).isEqualTo("7 days battery, heart rate");
        assertThat(new ProductSpecs.AccessorySpecs(List.of(Category.PHONE, Category.TABLET)).summary())
                .isEqualTo("Fits Phones, Tablets");
    }

    @Test
    void aProductWithoutVariantsHasNoStartingPrice() {
        Phone draft = new Phone("Nexora Phone 16", NEXORA, "Coming soon.", 6.3, 4700, true);

        ProductResponse response = ProductResponse.from(draft);

        assertThat(response.startingPrice()).isNull();
        assertThat(response.available()).isFalse();
        assertThat(response.variants()).isEmpty();
    }

    @Test
    void theJsonLeavesOutMissingValuesAndKeepsAmountsAsText() {
        Accessory charger = new Accessory("Nexora 30W Charger", NEXORA, "A charger.", Set.of(Category.PHONE));
        charger.addVariant(new ProductVariant(new Sku("NX-CHG30-WHT"), "White", null, Money.euros("39"), 80));

        String json = JsonMapper.builder().build().writeValueAsString(ProductResponse.from(charger));

        assertThat(json)
                .doesNotContain("storageGb")              // @JsonInclude(NON_NULL)
                .contains("\"amount\":\"39.00\"")         // a string, not a number
                .contains("\"category\":\"ACCESSORY\"")   // enums as their name
                .doesNotContain("\"stock\"");             // stock levels stay internal
    }
}