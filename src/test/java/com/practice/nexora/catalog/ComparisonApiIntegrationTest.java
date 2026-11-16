package com.practice.nexora.catalog;

import com.practice.nexora.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql("/db/dev/R__dev_catalog.sql")
class ComparisonApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private JdbcTemplate jdbc;

    private Product a;
    private Product b;
    private Product otherCategory;

    @BeforeEach
    void pickProductsFromTheDemoCatalog() {
        Map<Category, List<Product>> byCategory = products.findAll().stream()
                .collect(Collectors.groupingBy(Product::getCategory));

        List<List<Product>> buckets = new ArrayList<>(byCategory.values());
        List<Product> sameCategory = buckets.stream()
                .filter(bucket -> bucket.size() >= 2)
                .findFirst().orElseThrow();
        a = sameCategory.get(0);
        b = sameCategory.get(1);
        otherCategory = buckets.stream()
                .filter(bucket -> bucket != sameCategory)
                .findFirst().orElseThrow()
                .getFirst();
    }


    @AfterEach
    void emptyTheDatabase() {
        jdbc.execute("TRUNCATE brands, user_accounts CASCADE");
    }

    private static String slugOf(Product product) {
        return product.getSlug().value();
    }

    @Test
    void comparesTwoProducts_inTheRequestedOrder_withoutAToken() throws Exception {
        mockMvc.perform(get("/api/products/compare").param("slugs", slugOf(b) + "," + slugOf(a)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0]").value(slugOf(b)))
                .andExpect(jsonPath("$.products[1]").value(slugOf(a)))
                .andExpect(jsonPath("$.table.Product", hasSize(2)))
                .andExpect(jsonPath("$.table.Product[0]").value(b.getName()))
                .andExpect(jsonPath("$.table['Price from']", hasSize(2)))
                .andExpect(jsonPath("$.category").value(a.getCategory().getDisplayName()));
    }

    @Test
    void theCheapestIsOneOfTheComparedProducts() throws Exception {
        mockMvc.perform(get("/api/products/compare").param("slugs", slugOf(a) + ", " + slugOf(b)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cheapest").value(oneOf(slugOf(a), slugOf(b))));
    }

    @Test
    void differentCategoriesAreABadRequest() throws Exception {
        mockMvc.perform(get("/api/products/compare").param("slugs", slugOf(a) + "," + slugOf(otherCategory)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Only products of the same category can be compared"));
    }

    @Test
    void oneProductIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/products/compare").param("slugs", slugOf(a)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("2 to 4")));
    }

    @Test
    void theSameProductTwiceIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/products/compare").param("slugs", slugOf(a) + "," + slugOf(a)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The same product appears more than once"));
    }

    @Test
    void moreThanFourIsRejectedBeforeTheDatabaseIsAsked() throws Exception {
        // These slugs don't exist: a 400 (not 404) proves the size guard ran first.
        mockMvc.perform(get("/api/products/compare").param("slugs", "p1,p2,p3,p4,p5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnknownSlugIsNotFound() throws Exception {
        mockMvc.perform(get("/api/products/compare").param("slugs", slugOf(a) + ",no-such-product"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("no-such-product")));
    }

    @Test
    void withoutSlugsItIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/products/compare"))
                .andExpect(status().isBadRequest());
    }
}