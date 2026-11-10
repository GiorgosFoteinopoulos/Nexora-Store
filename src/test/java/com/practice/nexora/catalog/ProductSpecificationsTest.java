package com.practice.nexora.catalog;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductSpecificationsTest {

    private static final PageRequest FIRST_TEN_BY_NAME = PageRequest.of(0, 10, Sort.by("name"));

    @Autowired
    private ProductRepository products;

    @Autowired
    private BrandRepository brands;

    @BeforeEach
    void saveCatalog() {
        Brand nexora = brands.save(new Brand("Nexora"));
        Brand orbit = brands.save(new Brand("Orbit"));

        Phone phone15 = new Phone("Nexora Phone 15", nexora, "A phone.", 6.1, 4500, true);
        phone15.addVariant(variant("NX-PH15-128-BLK", 128, "849")); // two variants inside 800-1000:
        phone15.addVariant(variant("NX-PH15-256-BLK", 256, "949")); // a JOIN would return this phone twice

        Phone phone15Pro = new Phone("Nexora Phone 15 Pro", nexora, "The bigger one.", 6.7, 5000, true);
        phone15Pro.addVariant(variant("NX-PH15P-256-BLK", 256, "1099"));

        Phone orbitOne = new Phone("Orbit One", orbit, "A budget phone.", 6.5, 5000, false);
        orbitOne.addVariant(variant("OR-ONE-64-BLU", 64, "499"));

        products.saveAll(List.of(phone15, phone15Pro, orbitOne));
    }

    private static ProductVariant variant(String sku, int storageGb, String euros) {
        return new ProductVariant(new Sku(sku), "Black", storageGb, Money.euros(euros), 5);
    }

    private List<String> namesMatching(ProductFilter filter) {
        return products.findAll(ProductSpecifications.matching(filter), FIRST_TEN_BY_NAME)
                .map(Product::getName)
                .getContent();
    }

    private static ProductFilter filter(Category category, String brand, String min, String max, String q) {
        return new ProductFilter(category, brand,
                min == null ? null : new BigDecimal(min), max == null ? null : new BigDecimal(max), q);
    }

    @Test
    void noFilterReturnsEverything() {
        assertThat(namesMatching(ProductFilter.none()))
                .containsExactly("Nexora Phone 15", "Nexora Phone 15 Pro", "Orbit One");
    }

    @Test
    void filtersByBrandSlug() {
        assertThat(namesMatching(filter(null, "orbit", null, null, null))).containsExactly("Orbit One");
    }

    @ParameterizedTest(name = "brand \"{0}\" matches nothing")
    @ValueSource(strings = {"missing", "Not A Slug"})
    void anUnknownOrInvalidBrandMatchesNothing(String brand) {
        assertThat(namesMatching(filter(null, brand, null, null, null))).isEmpty();
    }

    @Test
    void filtersByCategory() {
        assertThat(namesMatching(filter(Category.PHONE, null, null, null, null))).hasSize(3);
    }

    @Test
    void aCategoryWithNoProductsMatchesNothing() {
        assertThat(namesMatching(filter(Category.LAPTOP, null, null, null, null))).isEmpty();
    }

    @Test
    void aPriceRangeReturnsEachProductOnce() {
        assertThat(namesMatching(filter(null, null, "800", "1000", null))).containsExactly("Nexora Phone 15");
    }

    @Test
    void onlyAMinimumPrice() {
        assertThat(namesMatching(filter(null, null, "1000", null, null))).containsExactly("Nexora Phone 15 Pro");
    }

    @Test
    void onlyAMaximumPrice() {
        assertThat(namesMatching(filter(null, null, null, "500", null))).containsExactly("Orbit One");
    }

    @Test
    void textSearchInTheNameIgnoresCase() {
        assertThat(namesMatching(filter(null, null, null, null, "PRO"))).containsExactly("Nexora Phone 15 Pro");
    }

    @Test
    void textSearchAlsoLooksInTheDescription() {
        assertThat(namesMatching(filter(null, null, null, null, "budget"))).containsExactly("Orbit One");
    }

    @Test
    void aPercentSignIsSearchLiterally() {
        assertThat(namesMatching(filter(null, null, null, null, "%"))).isEmpty();
    }

    @Test
    void filtersCombineWithAnd() {
        assertThat(namesMatching(filter(null, "nexora", null, "900", null))).containsExactly("Nexora Phone 15");
    }

    @Test
    void returnsOnePageWithTotals() {
        Page<Product> firstPage = products.findAll(
                ProductSpecifications.matching(ProductFilter.none()), PageRequest.of(0, 2, Sort.by("name"))
        );

        assertThat(firstPage.getContent()).extracting(Product::getName)
                .containsExactly("Nexora Phone 15", "Nexora Phone 15 Pro");
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }
}
