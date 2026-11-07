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
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductVariantPersistenceTest {

    private static final Slug PHONE_SLUG = new Slug("nexora-phone-15");


    @Autowired
    private ProductRepository products;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private TestEntityManager entityManager;

    private Brand nexora;

    @BeforeEach
    void saveBrand() {
        nexora = brands.save(new Brand("Nexora"));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private long variantRows() {
        return ((Number) entityManager.getEntityManager()
                .createNativeQuery("SELECT count(*) FROM product_variants")
                .getSingleResult()).longValue();
    }

    private static ProductVariant variant(String sku, Integer storageGb, String price, int stock) {
        return new ProductVariant(new Sku(sku), "Black", storageGb, Money.euros(price), stock);
    }

    private Phone phoneWithTwoVariants() {
        Phone phone = new Phone("Nexora Phone 15", nexora, "A phone", 6.1, 5400, true);
        phone.addVariant(variant("NX-PH15-128-BLK", 128, "799", 5));
        phone.addVariant(variant("NX-PH15-256-BLK", 256, "899", 0));
        return phone;
    }

    private Product reload() {
        return products.findBySlug(PHONE_SLUG).orElseThrow();
    }

    @Test
    void savingTheProductSavesItsVariants() {
        products.save(phoneWithTwoVariants());
        flushAndClear();

        assertThat(reload().getVariants())
                .extracting(variant -> variant.getSku().value())
                .containsExactly("NX-PH15-128-BLK", "NX-PH15-256-BLK");
    }

    @Test
    void aVariantComesBackWithItsPriceStockAndProduct() {
        products.save(phoneWithTwoVariants());
        flushAndClear();

        Product phone = reload();
        ProductVariant variant = phone.findVariant(new Sku("NX-PH15-256-BLK")).orElseThrow();

        assertThat(variant.getPrice()).isEqualTo(Money.euros("899")); // Money rebuilt from two columns
        assertThat(variant.getStock()).isZero();
        assertThat(variant.getStorageGb()).contains(256);
        assertThat(variant.getProduct()).isEqualTo(phone);
    }

    @Test
    void theDomainRulesWorkOnALoadedProduct() {
        products.save(phoneWithTwoVariants());
        flushAndClear();

        Product phone = reload();

        assertThat(phone.getStartingPrice()).contains(Money.euros("799"));
        assertThat(phone.isAvailable()).isTrue();
        assertThat(phone.getTotalStock()).isEqualTo(5);
    }

    @Test
    void changesToAVariantAreSavedWithoutCallingSave() {
        products.save(phoneWithTwoVariants());
        flushAndClear();

        reload().findVariant(new Sku("NX-PH15-128-BLK")).orElseThrow().removeFromStock(2);
        flushAndClear(); // dirty checking: Hibernate writes the UPDATE by itself

        assertThat(reload().findVariant(new Sku("NX-PH15-128-BLK")).orElseThrow().getStock()).isEqualTo(3);
    }

    @Test
    void removingAVariantFromTheProductDeletesItsRow() {
        products.save(phoneWithTwoVariants());
        flushAndClear();

        reload().removeVariant(new Sku("NX-PH15-256-BLK"));
        flushAndClear(); // orphanRemoval: DELETE

        assertThat(variantRows()).isEqualTo(1);
    }

    @Test
    void deletingTheProductDeletesItsVariants() {
        products.save(phoneWithTwoVariants());
        flushAndClear();

        products.delete(reload());
        flushAndClear(); // cascade REMOVE

        assertThat(variantRows()).isZero();
    }

    @Test
    void theDatabaseRejectsTheSameSkuOnTwoProducts() {
        products.saveAndFlush(phoneWithTwoVariants());
        Phone other = new Phone("Nexora Phone 16", nexora, "Another phone.", 6.3, 4700, true);
        other.addVariant(variant("NX-PH15-128-BLK", 128, "899", 1)); // same SKU as on the first phone

        assertThatThrownBy(() -> products.saveAndFlush(other))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
