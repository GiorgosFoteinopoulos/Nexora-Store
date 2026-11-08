package com.practice.nexora.catalog;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.Money;
import com.practice.nexora.common.Slug;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


@DataJpaTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class DevCatalogTest {

    @Autowired
    private ProductRepository products;

    @Autowired
    private BrandRepository brands;

    @Test
    void loadsTheWholeDemoCatalog() {
        assertThat(brands.count()).isEqualTo(3);
        assertThat(products.count()).isEqualTo(14);
        assertThat(products.findAllOfKind(Phone.class)).hasSize(4);
        assertThat(products.findAllOfKind(Laptop.class)).hasSize(3);
        assertThat(products.findAllOfKind(Tablet.class)).hasSize(2);
        assertThat(products.findAllOfKind(Wearable.class)).hasSize(2);
        assertThat(products.findAllOfKind(Accessory.class)).hasSize(3);
    }

    @Test
    void everyProductHasVariantsAndSensibleSpecifications() {
        assertThat(products.findAll()).allSatisfy(product -> {
            assertThat(product.getVariants()).as("variants of %s", product.getName()).isNotEmpty();
            switch (product) {
                case Phone phone -> assertThat(phone.getScreenInches()).isBetween(3.0, 8.0);
                case Laptop laptop -> assertThat(laptop.getScreenInches()).isBetween(10.0, 18.0);
                case Tablet tablet -> assertThat(tablet.getScreenInches()).isBetween(7.0, 14.0);
                case Wearable wearable -> assertThat(wearable.getBatteryLifeDays()).isPositive();
                case Accessory accessory -> assertThat(accessory.getCompatibleWith()).isNotEmpty();
                default -> fail("Unknown kind of product: " + product.getName());
            }
        });
    }

    @Test
    void theDomainRulesWorkOnTheDemoData() {
        Product phone = products.findBySlug(new Slug("nexora-phone-15")).orElseThrow();
        assertThat(phone.getVariants()).hasSize(3);
        assertThat(phone.getStartingPrice()).contains(Money.euros("799"));
        assertThat(phone.getTotalStock()).isEqualTo(37); // 25 + 12 + 0

        Product hub = products.findBySlug(new Slug("voltix-usb-c-hub")).orElseThrow();
        assertThat(hub).isInstanceOfSatisfying(Accessory.class, accessory ->
                assertThat(accessory.getCompatibleWith())
                        .containsExactlyInAnyOrder(Category.LAPTOP, Category.TABLET));
    }
}