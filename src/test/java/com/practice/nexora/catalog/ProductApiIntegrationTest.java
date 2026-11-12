package com.practice.nexora.catalog;

import com.practice.nexora.TestcontainersConfiguration;
import com.practice.nexora.common.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private BrandRepository brands;

    @BeforeEach
    void saveAPhone() {
        Brand nexora = brands.save(new Brand("Nexora"));
        Phone phone = new Phone("Nexora Phone 15", nexora, "A phone.", 6.1, 4500, true);
        phone.addVariant(new ProductVariant(new Sku("NX-PH15-128-BLK"), "Black", 128, Money.euros("799"), 5));
        products.save(phone);
    }

    @AfterEach
    void cleanUp() {
        products.deleteAll();
        brands.deleteAll();
    }


    @Test
    void servesAProductFromTheRealDatabase() throws Exception {
        mvc.perform(get("/api/products/nexora-phone-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand.name").value("Nexora"))   // the lazy brand, loaded inside the service
                .andExpect(jsonPath("$.variants.length()").value(1));  // the lazy variants, too
    }

    @Test
    void anUnknownSlugIsA404() throws Exception {
        mvc.perform(get("/api/products/nexora-phone-99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No product with slug 'nexora-phone-99'"));
    }

    @Test
    void listsProductsFilteredByBrand() throws Exception {
        mvc.perform(get("/api/products").param("brand", "nexora"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].brand.name").value("Nexora"))
                .andExpect(jsonPath("$.content[0].startingPrice.amount").value("799.00"));
    }
}
