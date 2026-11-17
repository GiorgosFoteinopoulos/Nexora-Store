package com.practice.nexora.advisor;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.catalog.Category;
import com.practice.nexora.catalog.ComparisonResponse;
import com.practice.nexora.catalog.Product;
import com.practice.nexora.catalog.ProductRepository;
import com.practice.nexora.catalog.ProductSummaryResponse;
import com.practice.nexora.common.BadRequestException;
import com.practice.nexora.common.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class CatalogToolsIntegrationTest {

    @Autowired
    private CatalogTools tools;

    @Autowired
    private ProductRepository products;

    @Autowired
    private JdbcTemplate jdbc;

    private Category category;
    private List<Product> ofThatCategory;

    @BeforeEach
    void pickACategory() {
        Map<Category, List<Product>> byCategory = products.findAll().stream()
                .collect(Collectors.groupingBy(Product::getCategory));
        Map.Entry<Category, List<Product>> entry = byCategory.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .findFirst().orElseThrow();
        category = entry.getKey();
        ofThatCategory = entry.getValue();
    }


    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    private static List<String> slugs(List<ProductSummaryResponse> results) {
        return results.stream().map(ProductSummaryResponse::slug).toList();
    }

    @Test
    void searchByCategoryReturnsOnlyThatCategory() {
        List<ProductSummaryResponse> results =
                tools.searchProducts(category.name().toLowerCase(Locale.ROOT), null, null, null, null);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(result -> result.category() == category);
    }

    @Test
    void theCategoryIsForgiving() {
        List<String> strict = slugs(tools.searchProducts(category.name(), null, null, null, null));

        assertThat(slugs(tools.searchProducts(category.name().toLowerCase(Locale.ROOT) + "s", null, null, null, null)))
                .isEqualTo(strict);
        assertThat(slugs(tools.searchProducts(category.getDisplayName().toUpperCase(Locale.ROOT), null, null, null, null)))
        .isEqualTo(strict);
    }

    @Test
    void anUnknownCategoryExplainsWhatIsAllowed() {
        assertThatThrownBy(() -> tools.searchProducts("phonez", null, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unknown category 'phonez'")
                .hasMessageContaining(category.name().toLowerCase(Locale.ROOT));
    }

    @Test
    void theBrandIsForgiving() {
        List<String> strict = slugs(tools.searchProducts(null, "nexora", null, null, null));

        assertThat(strict).isNotEmpty();
        assertThat(slugs(tools.searchProducts(null, "  Nexora ", null, null, null))).isEqualTo(strict);
    }

    @Test
    void resultsAreCappedSoTheModelIsNotFlooded() {
        assertThat(products.count()).isGreaterThan(CatalogTools.MAX_RESULTS);

        assertThat(tools.searchProducts(null, null, null, null, null)).hasSize(CatalogTools.MAX_RESULTS);
    }

    @Test
    void theMaximumPriceReallyFilters() {
        assertThat(tools.searchProducts(null, null, null, new BigDecimal("0.01"), null)).isEmpty();
        assertThat(tools.searchProducts(null, null, null, new BigDecimal("100000"), null)).isNotEmpty();
    }

    @Test
    void impossiblePricesAreRejectedWithAClearMessage() {
        assertThatThrownBy(() -> tools.searchProducts(null, null, new BigDecimal("-1"), null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("minPrice must not be negative");
        assertThatThrownBy(() -> tools.searchProducts(null, null, new BigDecimal("900"), new BigDecimal("500"), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("minPrice must not be greater than maxPrice");
    }
    

    @Test
    void detailsOfAnUnknownProductSayWhichSlug() {
        assertThatThrownBy(() -> tools.getProductDetails("no-such-product"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no-such-product");
    }

    @Test
    void compareIsForgivingAboutSpacesAndCapitals() {
        String a = ofThatCategory.get(0).getSlug().value();
        String b = ofThatCategory.get(1).getSlug().value();

        ComparisonResponse comparison = tools.compareProducts(new ArrayList<>(List.of(" " + a.toUpperCase(Locale.ROOT), b)));

        assertThat(comparison.products()).containsExactly(a, b);
    }
}