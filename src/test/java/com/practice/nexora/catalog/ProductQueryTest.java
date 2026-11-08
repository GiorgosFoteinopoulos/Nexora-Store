package com.practice.nexora.catalog;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.Money;
import com.practice.nexora.common.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductQueryTest {

    @Autowired
    private ProductRepository products;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void saveASmallCatalog() {
        Brand nexora = brands.save(new Brand("Nexora"));
        Brand voltix = brands.save(new Brand("Voltix"));

        Phone nexoraPhone = new Phone("Nexora Phone 15", nexora, "A phone.", 6.1, 4500, true);
        nexoraPhone.addVariant(variant("NX-PH15-128-BLK", "799", 5));

        Phone voltixPhone = new Phone("Voltix V9", voltix, "A phone.", 6.5, 5200, true);
        voltixPhone.addVariant(variant("VX-V9-128-BLU", "549", 4));
        voltixPhone.addVariant(variant("VX-V9-256-BLU", "629", 3)); // two cheap variants in stock

        Laptop laptop = new Laptop("Nexora Book 14", nexora, "A laptop.", 14.0, "Nexora N3", 16, 1.2);
        laptop.addVariant(variant("NX-BK14-512-SLV", "1299", 2));

        Accessory charger = new Accessory("Nexora 30W Charger", nexora, "A charger.",
                Set.of(Category.PHONE, Category.TABLET));
        charger.addVariant(new ProductVariant(new Sku("NX-CHG30-WHT"), "White", null, Money.euros("39"), 0));

        products.saveAll(List.of(nexoraPhone, voltixPhone, laptop, charger));
        entityManager.flush();
        entityManager.clear();
    }

    private static ProductVariant variant(String sku, String price, int stock) {
        return new ProductVariant(new Sku(sku), "Black", 128, Money.euros(price), stock);
    }

    private static List<String> names(List<Product> list) {
        return list.stream().map(Product::getName).toList();
    }

    @Test
    void findsTheProductsOfABrandBySlugSortedByName() {
        assertThat(names(products.findByBrand_SlugOrderByName(new Slug("nexora"))))
                .containsExactly("Nexora 30W Charger", "Nexora Book 14", "Nexora Phone 15");
    }

    @Test
    void findsAllProductsOfOneKind() {
        assertThat(names(products.findAllOfKind(Phone.class))).containsExactly("Nexora Phone 15", "Voltix V9");
        assertThat(names(products.findAllOfKind(Laptop.class))).containsExactly("Nexora Book 14");
    }

    @Test
    void findsProductsInStockAtOrBelowAPrice() {
        assertThat(names(products.findInStockAtOrBelow(new BigDecimal("600"))))
                .containsExactly("Voltix V9");
        assertThat(names(products.findInStockAtOrBelow(new BigDecimal("1000"))))
                .containsExactly("Nexora Phone 15", "Voltix V9");
    }

    @Test
    void eachProductAppearsOnceEvenWhenSeveralVariantsMatch() {
        // Both Voltix variants (549 and 629) match; the JOIN gives two rows, DISTINCT makes it one product.
        assertThat(names(products.findInStockAtOrBelow(new BigDecimal("700"))))
                .containsExactly("Voltix V9");
    }

    @Test
    void variantsOutOfStockDoNotCount() {

        assertThat(products.findInStockAtOrBelow(new BigDecimal("50"))).isEmpty();
    }
}