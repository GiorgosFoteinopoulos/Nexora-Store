package com.practice.nexora.catalog;


import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.Slug;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductRepositoryTest {

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

    private Phone phone(String name, Brand brand) {
        return new Phone(name, brand, "A phone.", 6.1, 4500, true);
    }

    @Test
    void savesAPhoneAndReadsItBackAsAPhone() {
        products.save(phone("Nexora Phone 15", nexora));
        flushAndClear();

        Product found = products.findBySlug(new Slug("nexora-phone-15")).orElseThrow();

        assertThat(found.getCategory()).isEqualTo(Category.PHONE);
        assertThat(found).isInstanceOfSatisfying(Phone.class, phone -> {
            assertThat(phone.getScreenInches()).isEqualTo(6.1);
            assertThat(phone.getBatteryMah()).isEqualTo(4500);
            assertThat(phone.isFiveG()).isTrue();
        });
    }

    @Test
    void oneRepositoryReturnsEveryKindOfProduct() {
        products.save(phone("Nexora Phone 15", nexora));
        products.save(new Laptop("Nexora Book 14", nexora, "A laptop", 14.0, "Nexora N3", 16, 1.2));
        products.save(new Tablet("Nexora Tab 11", nexora, "A tablet.", 11.0, true, false));
        products.save(new Wearable("Nexora Watch S3", nexora, "A watch.", 7, true, true));
        products.save(new Accessory("Nexora 30W Charger", nexora, "A charger.", Set.of(Category.PHONE)));
        flushAndClear();

        assertThat(products.findAll())
                .extracting(Product::getCategory)
                .containsExactlyInAnyOrder(Category.PHONE, Category.LAPTOP, Category.TABLET,
                        Category.WEARABLE, Category.ACCESSORY);
    }

    @Test
    void anAccessoryKeepsItsCompatibleCategories() {
        products.save(new Accessory("Nexora 30W Charger", nexora, "A charger.",
                Set.of(Category.PHONE, Category.TABLET)));
        flushAndClear();

        Product found = products.findBySlug(new Slug("nexora-30w-charger")).orElseThrow();

        assertThat(found).isInstanceOfSatisfying(Accessory.class, accessory ->
                assertThat(accessory.getCompatibleWith())
                        .containsExactlyInAnyOrder(Category.PHONE, Category.TABLET));
    }

    @Test
    void theBrandIsLoadedOnlyWhenFirstUsed() {
        Product saved = products.save(phone("Nexora Phone 15", nexora));
        flushAndClear();

        Product found = products.findById(saved.getId()).orElseThrow();

        assertThat(Hibernate.isInitialized(found.getBrand())).isFalse();
        assertThat(found.getBrand().getName()).isEqualTo("Nexora");
        assertThat(Hibernate.isInitialized(found.getBrand())).isTrue();
    }

    @Test
    void theProductPageQueryFetchesTheBrandUpFront() {
        products.save(phone("Nexora Phone 15", nexora));
        flushAndClear();


        Product found = products.findBySlug(new Slug("nexora-phone-15")).orElseThrow();

        assertThat(Hibernate.isInitialized(found.getBrand())).isTrue();
    }

    @Test
    void findsTheProductsOfABrand() {
        Brand voltix = brands.save(new Brand("Voltix"));
        products.save(phone("Nexora Phone 15", nexora));
        products.save(phone("Nexora Phone 16", nexora));
        products.save(phone("Voltix V9", voltix));
        flushAndClear();

        assertThat(products.findByBrand(nexora))
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Nexora Phone 15", "Nexora Phone 16");
    }

    @Test
    void hibernateWritesTheCategoryColumnByItself() {
        products.save(new Laptop("Nexora Book 14", nexora, "A laptop.", 14.0, "Nexora N3", 16, 1.2));
        flushAndClear();

        Object Category = entityManager.getEntityManager()
                .createNativeQuery("SELECT category FROM products WHERE slug = 'nexora-book-14'")
                .getSingleResult();
        assertThat(Category).isEqualTo("LAPTOP");
    }
}
